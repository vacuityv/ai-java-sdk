package me.vacuity.ai.sdk.openai.decision.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token usage for a decision.
 * <p>
 * Only input tokens are billed on this endpoint; outputTokens and
 * reasoningTokens are reported for observability but carry no charge, and
 * neither do cache reads or writes.
 *
 * @author: vacuity
 * @create: 2026-10-08
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionUsage {

    @JsonProperty("input_tokens")
    private Integer inputTokens;

    @JsonProperty("input_tokens_details")
    private InputTokensDetails inputTokensDetails;

    @JsonProperty("output_tokens")
    private Integer outputTokens;

    @JsonProperty("output_tokens_details")
    private OutputTokensDetails outputTokensDetails;

    @JsonProperty("total_tokens")
    private Integer totalTokens;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InputTokensDetails {

        @JsonProperty("cached_tokens")
        private Integer cachedTokens;

        @JsonProperty("cache_write_tokens")
        private Integer cacheWriteTokens;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutputTokensDetails {

        @JsonProperty("reasoning_tokens")
        private Integer reasoningTokens;
    }
}
