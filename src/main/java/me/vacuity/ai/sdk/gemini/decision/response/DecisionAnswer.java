package me.vacuity.ai.sdk.gemini.decision.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * One answer. The three types share this shape, and each populates only its
 * own fields — check {@link #getType()} against
 * {@link me.vacuity.ai.sdk.gemini.decision.constant.DecisionConstant} before
 * reading one.
 *
 * @author vacuity
 * @since 2.5.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionAnswer {

    /** "noul", "choice" or "score"; matches the question's type. */
    private String type;

    /**
     * The top outcome's probability. For a yes/no answer it is
     * {@code max(noul, 1 - noul)}, so it never falls below 0.5 there.
     */
    private Double confidence;

    /**
     * noul only — the probability of yes, between 0.01 and 0.99. The
     * probability of no is 1 minus this; there is no separate field for it.
     */
    private Double noul;

    /** choice only — the winning option key. */
    private String choice;

    /**
     * score only — a probability-weighted average of the level indexes, which
     * start at 0, so it is fractional and is not itself an index. With five
     * levels, 3.11 sits just past the fourth.
     */
    private Double score;

    /**
     * choice and score — the full distribution, summing to 1. Keyed by option
     * key for a choice, and by level index rendered as a string ("0", "1", ...)
     * for a score.
     */
    private Map<String, Double> probabilities;

    /** score only — level index as a string to that level's label. */
    private Map<String, String> legend;
}
