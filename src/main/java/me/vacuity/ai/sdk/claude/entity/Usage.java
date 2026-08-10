package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 15:07
 **/


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
     * Which capacity served the request.
     */
    @JsonProperty("service_tier")
    private String serviceTier;
}
