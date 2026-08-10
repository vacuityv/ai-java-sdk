package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Structured output format — constrains the response to a JSON schema.
 * Replaces assistant-turn prefilling, which returns a 400 on current models.
 *
 * @author: vacuity
 * @create: 2026-08-10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonOutputFormat {

    /**
     * Always "json_schema".
     */
    private String type;

    /**
     * The JSON schema the response must conform to.
     */
    private Object schema;

    public static JsonOutputFormat jsonSchema(Object schema) {
        return JsonOutputFormat.builder().type("json_schema").schema(schema).build();
    }
}
