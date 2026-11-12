package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Log probabilities for a candidate, present when the request set
 * {@code responseLogprobs}.
 * <p>
 * Before 2.7.0 the SDK could send {@code responseLogprobs} / {@code logprobs}
 * but the response had nowhere to put this, so it was requested and dropped.
 *
 * @author vacuity
 * @since 2.7.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogprobsResult {

    private Double logProbabilitySum;

    /** The token actually chosen at each step. */
    private List<TokenLogprob> chosenCandidates;

    /** The most likely alternatives at each step. */
    private List<TopCandidates> topCandidates;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenLogprob {
        private String token;
        private Integer tokenId;
        private Double logProbability;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCandidates {
        private List<TokenLogprob> candidates;
    }
}
