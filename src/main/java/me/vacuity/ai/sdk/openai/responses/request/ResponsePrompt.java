package me.vacuity.ai.sdk.openai.responses.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Reference to a prompt template and its variables.
 *
 * @author: vacuity
 * @create: 2026-08-10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponsePrompt {

    /**
     * The unique identifier of the prompt template to use.
     */
    private String id;

    /**
     * Optional version of the prompt template.
     */
    private String version;

    /**
     * Optional map of values to substitute in for variables in your prompt.
     * The substitution values can either be strings, or other Response input
     * types like images or files.
     */
    private Map<String, Object> variables;
}
