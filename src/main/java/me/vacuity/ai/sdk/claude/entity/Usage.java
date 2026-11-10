package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token usage from the Claude Messages API.
 * <p>
 * Containment, which matters for billing: <strong>{@code inputTokens} is the
 * uncached remainder, not the total.</strong> The cache counts sit beside it
 * rather than inside it, so the whole prompt is
 * {@code inputTokens + cacheCreationInputTokens + cacheReadInputTokens}.
 * <p>
 * Measured on claude-opus-5-5 with a ~4.8k-token cached system prompt:
 * {@code inputTokens} was 13 on both calls of a pair, with 4763 under
 * {@code cacheCreationInputTokens} on the first and under
 * {@code cacheReadInputTokens} on the second — a 4776-token prompt either
 * way.
 * <p>
 * This is the opposite of the OpenAI-compatible
 * {@code me.vacuity.ai.sdk.openai.entity.Usage}, where {@code promptTokens}
 * is the total and the cache counts are subsets to subtract. Code that bills
 * both needs both formulas; the SDK reports each vendor's fields as that
 * vendor defines them and does not reconcile them for you.
 *
 * @author vacuity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usage {

    /**
     * Tokens processed at full price — the uncached remainder only.
     * Total prompt size is inputTokens + cacheCreationInputTokens + cacheReadInputTokens.
     */
    @JsonProperty("input_tokens")
    private Integer inputTokens;

    @JsonProperty("output_tokens")
    private Integer outputTokens;

    /**
     * Tokens written to the cache on this request (billed at ~1.25x for a 5m
     * TTL, ~2x for 1h).
     */
    @JsonProperty("cache_creation_input_tokens")
    private Integer cacheCreationInputTokens;

    /**
     * Tokens served from the cache on this request (billed at ~0.1x). If this
     * stays zero across repeated requests with the same prefix, something is
     * silently invalidating the cache.
     */
    @JsonProperty("cache_read_input_tokens")
    private Integer cacheReadInputTokens;

    /**
     * Breakdown of the billed output tokens — notably how many went to
     * internal reasoning.
     */
    @JsonProperty("output_tokens_details")
    private OutputTokensDetails outputTokensDetails;

    /**
     * Which capacity served the request.
     */
    @JsonProperty("service_tier")
    private String serviceTier;

    /**
     * Cache-write tokens split by lifetime. Needed to price writes at all,
     * since 5-minute and 1-hour writes cost 1.25x and 2x respectively.
     *
     * @since 2.3.0
     */
    @JsonProperty("cache_creation")
    private CacheCreation cacheCreation;

    /**
     * Where inference ran, for example "global". Informational.
     *
     * @since 2.3.0
     */
    @JsonProperty("inference_geo")
    private String inferenceGeo;

    /**
     * Which speed served the request: "standard" or "fast".
     * <p>
     * Billing-relevant — fast mode is priced at roughly 2x standard — and it
     * can differ from what was asked for, since a request may fall back to
     * standard. Bill what this says, not what was requested.
     *
     * @since 2.3.0
     */
    private String speed;
}
