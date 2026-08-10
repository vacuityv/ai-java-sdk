package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * How the model should use the provided tools.
 *
 * @author: vacuity
 * @create: 2026-08-10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolChoice {

    /**
     * One of "auto", "any", "tool", "none".
     */
    private String type;

    /**
     * The tool to call. Only used when type is "tool".
     */
    private String name;

    /**
     * Whether to force the model to use at most one tool per response.
     * Not accepted when type is "none".
     */
    @JsonProperty("disable_parallel_tool_use")
    private Boolean disableParallelToolUse;

    /**
     * The model decides whether to use tools (the default behaviour).
     */
    public static ToolChoice auto() {
        return ToolChoice.builder().type("auto").build();
    }

    /**
     * The model must use at least one tool.
     */
    public static ToolChoice any() {
        return ToolChoice.builder().type("any").build();
    }

    /**
     * The model must use the named tool.
     */
    public static ToolChoice tool(String name) {
        return ToolChoice.builder().type("tool").name(name).build();
    }

    /**
     * The model cannot use tools.
     */
    public static ToolChoice none() {
        return ToolChoice.builder().type("none").build();
    }
}
