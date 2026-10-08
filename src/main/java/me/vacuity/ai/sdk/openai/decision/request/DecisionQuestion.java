package me.vacuity.ai.sdk.openai.decision.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * One question put to the Decisions API.
 * <p>
 * Independent questions can share a single request; decisions that depend on
 * each other need separate requests.
 *
 * @author: vacuity
 * @create: 2026-10-08
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DecisionQuestion {

    /**
     * "predicate", "choice" or "score".
     */
    private String type;

    /**
     * Unique name, echoed back on the matching answer.
     */
    private String name;

    /**
     * What the model should decide.
     */
    private String instructions;

    /**
     * For "choice" only — the options to pick from.
     */
    private List<Choice> choices;

    /**
     * For "score" only — the ordered levels, lowest first.
     */
    private List<Level> levels;

    /**
     * Probability that the instructions hold true. The answer carries
     * {@code probability}.
     */
    public static DecisionQuestion predicate(String name, String instructions) {
        return DecisionQuestion.builder()
                .type("predicate").name(name).instructions(instructions).build();
    }

    /**
     * Pick one unordered category. The answer carries {@code choice},
     * {@code confidence} and the full {@code probabilities} distribution.
     */
    public static DecisionQuestion choice(String name, String instructions, List<Choice> choices) {
        return DecisionQuestion.builder()
                .type("choice").name(name).instructions(instructions).choices(choices).build();
    }

    /**
     * Rate against ordered levels. Pass levels lowest to highest — the answer's
     * {@code score} is a probability-weighted average of their indexes, so the
     * order is load-bearing.
     */
    public static DecisionQuestion score(String name, String instructions, List<Level> levels) {
        return DecisionQuestion.builder()
                .type("score").name(name).instructions(instructions).levels(levels).build();
    }

    /**
     * One option of a "choice" question.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Choice {

        /**
         * String or Boolean.
         */
        private Object value;

        private String description;

        public static Choice of(Object value, String description) {
            return Choice.builder().value(value).description(description).build();
        }
    }

    /**
     * One level of a "score" question.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Level {

        private String label;

        private String description;

        public static Level of(String label, String description) {
            return Level.builder().label(label).description(description).build();
        }
    }
}
