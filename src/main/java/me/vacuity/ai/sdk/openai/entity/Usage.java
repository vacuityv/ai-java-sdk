package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token usage from an OpenAI-compatible chat-completions response.
 * <p>
 * The fields mirror what each vendor documents, under that vendor's own wire
 * name, with no normalising layer on top: vendors report cache hits in two
 * different shapes and the SDK carries both rather than picking one. Which
 * fields arrive depends on the vendor and the model, so read the ones your
 * vendor documents.
 * <p>
 * Containment, which matters for billing and differs per vendor:
 * <ul>
 * <li>OpenAI: {@code promptTokens} is the total.
 * {@code promptTokensDetails.cachedTokens} and
 * {@code promptTokensDetails.cacheWriteTokens} are disjoint subsets of it, so
 * the ordinary remainder is {@code promptTokens} minus both. Measured on
 * gpt-5.6-terra: 2655 = 3 ordinary + 2652 written + 0 cached on one call, and
 * 3 + 0 + 2652 on the repeat.</li>
 * <li>DeepSeek: documents {@code promptTokens} =
 * {@code promptCacheHitTokens} + {@code promptCacheMissTokens}, and puts the
 * hit count in {@code promptTokensDetails.cachedTokens} as well — its docs
 * call that field "Same as prompt_cache_hit_tokens".</li>
 * <li>Alibaba Bailian: documents {@code promptTokensDetails.cachedTokens} as
 * "a part of" {@code promptTokens}. Its docs do not state how
 * {@code cacheCreationInputTokens} relates to {@code promptTokens}, so do not
 * assume — check against your own traffic before billing on it.</li>
 * </ul>
 * Note that {@code me.vacuity.ai.sdk.claude.entity.Usage} is the other way
 * round: there {@code inputTokens} is the uncached remainder and the cache
 * counts are added to it, not subtracted from it.
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
}
