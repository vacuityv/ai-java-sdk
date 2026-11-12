package me.vacuity.ai.sdk.gemini.decision.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request body for POST v1beta/decisions.
 * <p>
 * The Decisions API scores typed questions against shared input and returns
 * calibrated probabilities instead of text. It generates no output tokens at
 * all, so only input is billed.
 * <p>
 * Shaped the way Google documents it, which is not the way OpenAI documents
 * its own Decisions API: questions and answers are keyed maps rather than
 * lists, probabilities come back as a map, and yes/no is called "noul". No
 * attempt is made here to make the two look alike.
 *
 * @author vacuity
 * @since 2.5.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DecisionRequest {

    /** The evaluating model. Currently only "gemini-decision-1". */
    private String model;

    /**
     * The shared input every question is scored against. A String, a single
     * {@link DecisionInput}, or a List of them. Cannot be empty.
     * <p>
     * Context window is 262144 tokens across the input and every question.
     */
    private Object input;

    /**
     * The questions, keyed by an id echoed back on the matching answer. 1 to
     * 1000 entries; keys match {@code [A-Za-z0-9_-]+} up to 64 characters.
     */
    private Map<String, DecisionQuestion> questions;

    public static class DecisionRequestBuilder {

        /** Plain-text input. */
        public DecisionRequestBuilder input(String input) {
            this.input = input;
            return this;
        }

        /** A single input block. */
        public DecisionRequestBuilder input(DecisionInput input) {
            this.input = input;
            return this;
        }

        /** Several input blocks, for mixed text and media. */
        public DecisionRequestBuilder input(List<DecisionInput> input) {
            this.input = input;
            return this;
        }
    }
}
