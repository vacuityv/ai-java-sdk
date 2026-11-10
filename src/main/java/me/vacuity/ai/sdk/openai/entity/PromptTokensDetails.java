package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Breakdown of the prompt tokens, as returned by OpenAI-compatible
 * chat-completions endpoints.
 *
 * @author vacuity
 * @since 2.2.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTokensDetails {

    /**
     * Prompt tokens served from the context cache.
     * <p>
     * This is a <em>subset</em> of {@code promptTokens}, not an addition to
     * it: a measured call reported {@code prompt_tokens} 2658 with
     * {@code cached_tokens} 1792, so the tokens billed at the full rate were
     * 2658 - 1792. Adding the two would bill the cached part twice.
     * <p>
     * DeepSeek populates this with the same number it puts in
     * {@code promptCacheHitTokens}, so this one field covers OpenAI, Qwen and
     * DeepSeek alike — see {@link Usage#getCachedPromptTokens()}.
     */
    @JsonProperty("cached_tokens")
    private Integer cachedTokens;

    /** Audio tokens in the prompt. Reported as 0 on text-only calls. */
    @JsonProperty("audio_tokens")
    private Integer audioTokens;
}
