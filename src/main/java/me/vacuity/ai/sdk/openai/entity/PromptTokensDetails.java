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
     * {@code promptCacheHitTokens} — its own docs call it "Same as
     * prompt_cache_hit_tokens" — so a caller reading this field alone covers
     * OpenAI, Bailian and DeepSeek.
     */
    @JsonProperty("cached_tokens")
    private Integer cachedTokens;

    /** Audio tokens in the prompt. Reported as 0 on text-only calls. */
    @JsonProperty("audio_tokens")
    private Integer audioTokens;

    /**
     * Prompt tokens written into the cache by this request. OpenAI's name.
     * <p>
     * Measured on {@code gpt-5.6-terra}: the first call of a pair reported
     * {@code prompt_tokens} 2655 with {@code cache_write_tokens} 2652 and
     * {@code cached_tokens} 0; the second reported the same 2655 with the two
     * swapped. So writes and reads are both subsets of {@code promptTokens}
     * and never overlap — the ordinary remainder was 3 tokens either way.
     * <p>
     * It is <em>not</em> free: per OpenAI's caching guide a write costs 1.25x
     * the uncached input rate on GPT-5.6 and later (2.5x for the 12-hour
     * lifetime), while older models carry no write charge. So it is its own
     * line on a bill — billing a write at the plain input rate undercharges
     * it, and folding it in with the cached count overcharges it.
     * <p>
     * Absent on models that predate cache-write accounting — gpt-5.5 returns
     * only {@code cached_tokens} and {@code audio_tokens}.
     *
     * @since 2.3.0
     */
    @JsonProperty("cache_write_tokens")
    private Integer cacheWriteTokens;

    /**
     * Prompt tokens spent creating the cache. Alibaba Cloud Bailian's name for
     * the same thing, inside its OpenAI-compatible mode.
     * <p>
     * Documented at help.aliyun.com/zh/model-studio/context-cache, which
     * states the write rate as 125% of the input price for an explicit cache
     * and 100% for an implicit one. <strong>That page does not say how this
     * relates to {@code promptTokens}</strong>, and the SDK does not guess:
     * OpenAI's measured equivalent is a subset, but that is OpenAI's
     * behaviour, not a documented fact about Bailian. Check it against your
     * own traffic before billing on it.
     * <p>
     * Implemented from that documentation only: this machine has no Bailian
     * credentials, so unlike {@code cacheWriteTokens} it has no live
     * verification behind it.
     *
     * @since 2.3.0
     */
    @JsonProperty("cache_creation_input_tokens")
    private Integer cacheCreationInputTokens;

    /** Text tokens in the prompt. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("text_tokens")
    private Integer textTokens;
    /** Image tokens in the prompt. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("image_tokens")
    private Integer imageTokens;
}
