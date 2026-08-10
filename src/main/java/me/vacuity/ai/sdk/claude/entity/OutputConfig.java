package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for the model's output.
 *
 * @author: vacuity
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutputConfig {

    /**
     * Thinking depth and overall token spend:
     * "low", "medium", "high" (default), "xhigh", "max".
     */
    private String effort;

    /**
     * Constrains the response to a JSON schema.
     */
    private JsonOutputFormat format;

    /**
     * Token ceiling for an agentic loop, surfaced to the model so it paces
     * itself. Beta — send the "task-budgets-2026-03-13" flag.
     */
    @JsonProperty("task_budget")
    private TokenTaskBudget taskBudget;
}
