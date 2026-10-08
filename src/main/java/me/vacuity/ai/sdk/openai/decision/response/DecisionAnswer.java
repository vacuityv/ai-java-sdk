package me.vacuity.ai.sdk.openai.decision.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * One answer. The four answer types share this shape; check {@link #getType()}
 * against
 * {@link me.vacuity.ai.sdk.openai.decision.constant.DecisionConstant} before
 * reading a field, since each type only populates its own.
 *
 * @author: vacuity
 * @create: 2026-10-08
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionAnswer {

    /**
     * "predicate", "choice", "score" or "refusal".
     */
    private String type;

    /**
     * The name of the question this answers.
     */
    private String name;

    /**
     * predicate only — probability from 0 to 1 that the condition holds.
     */
    private Double probability;

    /**
     * choice only — the selected value. String or Boolean.
     */
    private Object choice;

    /**
     * score only — probability-weighted average of the level indexes, which
     * start at 0. With levels low/medium/high, 1.94 means "almost certainly
     * high". It is a continuous value, not a level index.
     */
    private Double score;

    /**
     * choice and score — how confident the model is in its pick.
     */
    private Double confidence;

    /**
     * choice and score — the full distribution over the options or levels.
     */
    private List<Probability> probabilities;

    /**
     * Whether the model declined to answer this question. Check this before
     * reading any value field: on a refusal they are all null.
     */
    public boolean isRefusal() {
        return "refusal".equals(type);
    }

    /**
     * One entry of the probability distribution.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Probability {

        /**
         * For a choice, the option value (String or Boolean). For a score, the
         * level index as an Integer.
         */
        private Object value;

        /**
         * score only — the level's label.
         */
        private String label;

        private Double probability;
    }
}
