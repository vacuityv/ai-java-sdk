package me.vacuity.ai.sdk.openai.responses.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for running moderation on the input and output of this response.
 *
 * @author: vacuity
 * @create: 2026-08-10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseModeration {

    /**
     * The moderation model to use for moderated completions,
     * e.g. "omni-moderation-latest".
     */
    private String model;

    /**
     * The policy to apply to moderated response input and output.
     */
    private Policy policy;

    /**
     * The policy to apply to moderated response input and output.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Policy {

        /**
         * The moderation policy for the response input.
         */
        private Mode input;

        /**
         * The moderation policy for the response output.
         */
        private Mode output;
    }

    /**
     * The moderation mode applied to input or output.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Mode {

        /**
         * Possible values: "score", "block".
         */
        private String mode;
    }
}
