package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:27
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageContent {

    private String type;

    private String text;
    
    private String thinking;
    
    private String signature;

    private ContentSource source;

    @JsonProperty("tool_use_id")
    private String toolUseId;

    private Object content;

    // only in function response
    private String id;

    private String name;

    private JsonNode input;

    @JsonProperty("partial_json")
    private String partialJson;

    /**
     * Cache breakpoint. Everything rendered before this block
     * (tools -> system -> messages) is cached.
     */
    @JsonProperty("cache_control")
    private CacheControl cacheControl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentSource {
        private String type;

        @JsonProperty("media_type")
        private String mediaType;

        private String data;
    }

    private String data;

    /**
     * Only on a message_delta event: why generation stopped — "end_turn",
     * "max_tokens", "tool_use", "pause_turn" or "refusal". Null on content
     * block deltas (text_delta / thinking_delta / input_json_delta /
     * signature_delta), which is every other delta you receive.
     */
    @JsonProperty("stop_reason")
    private String stopReason;

    /**
     * Only on a message_delta event: the stop sequence that triggered the stop,
     * when stopReason is "stop_sequence".
     */
    @JsonProperty("stop_sequence")
    private String stopSequence;

    /**
     * Retained so existing call sites using the previous all-args constructor
     * keep compiling after stopReason / stopSequence were added.
     */
    @Deprecated
    public ChatMessageContent(String type, String text, String thinking, String signature, ContentSource source, String toolUseId, Object content, String id, String name, JsonNode input, String partialJson, CacheControl cacheControl, String data) {
        this.type = type;
        this.text = text;
        this.thinking = thinking;
        this.signature = signature;
        this.source = source;
        this.toolUseId = toolUseId;
        this.content = content;
        this.id = id;
        this.name = name;
        this.input = input;
        this.partialJson = partialJson;
        this.cacheControl = cacheControl;
        this.data = data;
    }

    /** tool_result blocks: marks the tool call as failed, so Claude knows the content is an error rather than a result. Without it a failure reads as a successful answer. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("is_error")
    private Boolean isError;
    /** On a text block: the list of citations backing it. On a document block: {@code {"enabled": true}} to turn citations on — see {@link #citationsEnabled()}. One field for both because this class carries every block type. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("citations")
    private Object citations;
    /** document blocks: a title the model can cite the document by. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("title")
    private String title;
    /** document blocks: context about the document that is not itself citable. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("context")
    private String context;
    /** image blocks: transformations to apply before the model sees the image. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("transformations")
    private java.util.Map<String, Object> transformations;
    /** tool_use blocks: what invoked the tool, for programmatic tool calling. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("caller")
    private java.util.Map<String, Object> caller;
    /** tool_use / tool_result blocks: the MCP toolset the tool belongs to. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("toolset_name")
    private String toolsetName;
    /** Stream message_delta only: the refusal category when stop_reason is "refusal". @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("stop_details")
    private StopDetails stopDetails;
    /** Stream message_delta only: the code-execution container. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("container")
    private java.util.Map<String, Object> container;

    /**
     * The value that turns citations on for a document block:
     * {@code document.setCitations(ChatMessageContent.citationsEnabled())}.
     *
     * @since 2.7.0
     */
    public static java.util.Map<String, Object> citationsEnabled() {
        return java.util.Collections.<String, Object>singletonMap("enabled", Boolean.TRUE);
    }
}