package me.vacuity.ai.sdk.openai.responses.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration options for a text response from the model.
 *
 * @author: vacuity
 * @create: 2025-01-22
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseTextConfig {

    /**
     * An object specifying the format that the model must output.
     */
    private ResponseFormat format;

    /**
     * Constrains the verbosity of the model's response.
     * Possible values: "low", "medium", "high". Defaults to "medium".
     */
    private String verbosity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseFormat {
        /**
         * The type of response format. Can be "text", "json_object", or "json_schema".
         */
        private String type;

        /**
         * The name of the response format (required for json_schema).
         */
        private String name;

        /**
         * The JSON schema that the model must conform to (for json_schema type).
         */
        private Object schema;

        /**
         * Whether to enable strict schema adherence.
         */
        private Boolean strict;

        /**
         * Description of what the model should generate.
         */
        private String description;
    }
}
