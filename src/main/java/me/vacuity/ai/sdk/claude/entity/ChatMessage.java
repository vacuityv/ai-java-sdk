package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-02-22 14:28
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {

    private String role;

    private Object content;

    public ChatMessage(String role, String msg) {
        this.role = role;
        this.content = msg;
    }

    public ChatMessage(String role, List<ChatMessageContent> contents) {
        this.role = role;
        this.content = contents;
    }

    /**
     * Changes the effort level for the rest of the conversation. Beta — send
     * {@code "mid-conversation-output-config-2026-07-01"} in
     * {@code ChatRequest.betas}, or the server answers
     * {@code messages.N.output_config: Extra inputs are not permitted}.
     * <p>
     * Only valid on a {@code system} message whose {@code content} is empty.
     * Setting it on a message that carries text, or alongside
     * {@code clear_at}, is rejected:
     * {@code output_config is not permitted on a turn-scoped system message}.
     *
     * @since 2.4.0
     */
    @JsonProperty("output_config")
    private SystemMessageOutputConfig outputConfig;

    /**
     * A system message that carries no content and only changes the effort
     * level from this point on.
     * <p>
     * The point of it over a top-level {@code output_config.effort} change is
     * the cache: this leaves everything before it untouched, so the cached
     * prefix still matches, where a top-level change restarts the cache.
     * <p>
     * Placement: an effort-only message renders nothing and is accepted
     * anywhere in {@code messages}, first entry included — verified against
     * the live API. That exemption is specific to the empty-content form; a
     * system message carrying text has to follow the placement rules, and if
     * an effort-only message sits next to one that does carry content, both
     * are judged by those rules.
     * <p>
     * When it takes effect: placed directly after a {@code user} turn with new
     * input, that turn's reply already uses the new level. Anywhere else — for
     * example between an {@code assistant} turn and the next {@code user} turn
     * — it starts at the next {@code user} turn with new input, and a
     * {@code user} turn holding only {@code tool_result} blocks does not count
     * as new input. The level then holds until another message changes it.
     * <p>
     * Models: claude-fable-5-2, claude-mythos-5-2, claude-fable-5-1,
     * claude-mythos-5-1, claude-opus-5-5, claude-opus-5, claude-sonnet-5-5,
     * claude-haiku-5-5. Anything else answers {@code output_config.effort
     * requires a model that supports per-turn effort; this model does not}.
     * Two further rejections come from the thinking mode rather than the
     * model: Claude Sonnet 5.5 with {@code thinking.type} "between_tools", and
     * Claude Haiku 5.5 with "disabled", cannot change effort mid-conversation.
     * <p>
     * On models with preserved thinking, moving or removing this message after
     * the fact invalidates the thinking blocks in every later
     * {@code assistant} turn — append it, do not rewrite history around it.
     *
     * @param effort "low", "medium", "high", "xhigh" or "max"
     * @since 2.4.0
     */
    public static ChatMessage effortOnly(String effort) {
        ChatMessage message = new ChatMessage();
        message.setRole("system");
        // An empty list, not null and not omitted: the API wants content to be
        // present and empty. The client mapper uses NON_NULL inclusion, so an
        // empty list really does go out as [].
        message.setContent(new java.util.ArrayList<ChatMessageContent>());
        message.setOutputConfig(SystemMessageOutputConfig.effort(effort));
        return message;
    }

    /**
     * Makes a mid-conversation {@code system} message turn-scoped: it renders
     * for one turn and then stays in the transcript, cleared.
     * <p>
     * "next_user_message" or "never" (the default when absent) — the server
     * names both when it rejects anything else. Beta: send
     * {@code "mid-conversation-system-clear-at-2026-08-21"} in
     * {@code ChatRequest.betas}, or it answers
     * {@code messages.N.clear_at: Extra inputs are not permitted}.
     * <p>
     * Two things are refused on a turn-scoped message, both measured:
     * {@code cache_control} on its content blocks, and {@code output_config}
     * — so this cannot be combined with
     * {@link #effortOnly(String)}. A plain mid-conversation system message,
     * without {@code clear_at}, <em>can</em> carry {@code cache_control} and
     * caches normally.
     * <p>
     * Placement is the same as any system message that carries content: it
     * must follow a {@code user} turn (or an {@code assistant} turn ending in
     * a server tool result) and must precede an {@code assistant} turn or end
     * the array. As {@code messages[0]} the server points you at the
     * top-level {@code system} parameter instead.
     * <p>
     * The reason to reach for it: a reminder injected into history and removed
     * on the next request is a history edit, which breaks the cache from that
     * point and, on models with preserved thinking, invalidates later thinking
     * blocks. Appending a turn-scoped copy each turn and leaving the old ones
     * in place avoids both.
     *
     * @since 2.6.0
     */
    @JsonProperty("clear_at")
    private String clearAt;

    /**
     * A system message that renders for one turn only, then stays in the
     * transcript cleared. Append a fresh one each turn rather than deleting
     * the previous.
     *
     * @since 2.6.0
     */
    public static ChatMessage turnScopedSystem(String text) {
        ChatMessage message = new ChatMessage();
        message.setRole("system");
        message.setContent(text);
        message.setClearAt("next_user_message");
        return message;
    }
}
