package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token usage from an OpenAI-compatible chat-completions response.
 * <p>
 * Shared by every vendor the SDK reaches through that wire format, which
 * report cache hits in two different shapes. Both are carried here; prefer
 * {@link #getCachedPromptTokens()} over reading either directly.
 * <p>
 * In streaming, usage does not arrive unless it is asked for: against OpenAI,
 * without {@code streamOptions.includeUsage = true} no chunk carries usage at
 * all — not even the last one — and with it set, a final usage-only chunk is
 * emitted while every earlier chunk carries {@code usage: null}. DeepSeek
 * instead documents usage riding on the last content chunk with no separate
 * usage-only chunk. Code that bills from a stream has to tolerate both.
 *
 * @author: vacuity
 * @create: 2024-05-16 17:43
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usage {

    @JsonProperty("completion_tokens")
    private Integer completionTokens;

    @JsonProperty("prompt_tokens")
    private Integer promptTokens;

    @JsonProperty("total_tokens")
    private Integer totalTokens;

    /**
     * Prompt tokens that hit the context cache. DeepSeek's shape.
     * <p>
     * DeepSeek documents {@code promptTokens} = this + {@code
     * promptCacheMissTokens}, and puts the same number in
     * {@code promptTokensDetails.cachedTokens}. Null on vendors that do not
     * report it, OpenAI among them.
     *
     * @since 2.2.1
     */
    @JsonProperty("prompt_cache_hit_tokens")
    private Integer promptCacheHitTokens;

    /**
     * Prompt tokens that missed the context cache. DeepSeek's shape; null
     * elsewhere.
     *
     * @since 2.2.1
     */
    @JsonProperty("prompt_cache_miss_tokens")
    private Integer promptCacheMissTokens;

    /**
     * The standard breakdown of the prompt tokens, which is where OpenAI,
     * Qwen and DeepSeek all report cached tokens.
     *
     * @since 2.2.1
     */
    @JsonProperty("prompt_tokens_details")
    private PromptTokensDetails promptTokensDetails;

    /**
     * The standard breakdown of the completion tokens.
     *
     * @since 2.2.1
     */
    @JsonProperty("completion_tokens_details")
    private CompletionTokensDetails completionTokensDetails;

    /**
     * Prompt tokens served from cache, whichever shape the vendor used, or
     * null when none reported any.
     * <p>
     * Reads {@code promptTokensDetails.cachedTokens} first and falls back to
     * {@code promptCacheHitTokens}; DeepSeek sets both to the same value, so
     * the order only matters for vendors that set one. Derived, not a wire
     * field.
     *
     * @since 2.2.1
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getCachedPromptTokens() {
        if (promptTokensDetails != null && promptTokensDetails.getCachedTokens() != null) {
            return promptTokensDetails.getCachedTokens();
        }
        return promptCacheHitTokens;
    }

    /**
     * Prompt tokens that were NOT served from cache — the part billed at the
     * full input rate — or null when the split is unknown.
     * <p>
     * Computed as {@code promptTokens - cachedPromptTokens}, because cached
     * tokens are a subset of {@code promptTokens} rather than a separate
     * bucket. Billing both {@code promptTokens} and the cached count charges
     * the cached part twice.
     *
     * @since 2.2.1
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getUncachedPromptTokens() {
        Integer cached = getCachedPromptTokens();
        if (promptTokens == null) {
            return null;
        }
        if (cached == null) {
            return promptTokens;
        }
        return promptTokens - cached;
    }
}
