package me.vacuity.ai.sdk.openai.decision.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.responses.entity.ResponseInputItem;

import java.util.List;

/**
 * Request body for POST /v1/decisions.
 * <p>
 * The Decisions API returns a typed choice with probabilities instead of free
 * text. Only gpt-6-luna supports it, and only input tokens are billed.
 *
 * @author: vacuity
 * @create: 2026-10-08
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DecisionRequest {

    /**
     * The evaluating model. Currently only "gpt-6-luna".
     */
    private String model;

    /**
     * The shared evidence every question is answered against. Either a String,
     * or a List of {@link ResponseInputItem} carrying input_text / input_image
     * parts.
     * <p>
     * Images must be inline base64 data URLs — hosted http(s) URLs and file ids
     * are rejected by this endpoint.
     */
    private Object input;

    /**
     * The questions to answer. Independent questions can share one request.
     */
    private List<DecisionQuestion> questions;

    /**
     * Stable identifier used to detect policy-violating users.
     */
    @JsonProperty("safety_identifier")
    private String safetyIdentifier;

    public static class DecisionRequestBuilder {

        /**
         * Plain-text evidence.
         */
        public DecisionRequestBuilder input(String input) {
            this.input = input;
            return this;
        }

        /**
         * Evidence as message items, for image or multi-part input.
         */
        public DecisionRequestBuilder input(List<ResponseInputItem> input) {
            this.input = input;
            return this;
        }
    }
}
