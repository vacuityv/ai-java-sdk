package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-12-16 14:48
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolConfig {

    private FunctionCallingConfig functionCallingConfig;

    /**
     * Required when a request combines function declarations with a built-in
     * tool such as googleSearch. Without it, gemini-4-argon rejects the request
     * with:
     * "Please enable tool_config.include_server_side_tool_invocations to use
     * Built-in tools with Function calling."
     * <p>
     * Turning it on makes the model return its server-side tool activity as
     * extra parts — see {@link ChatMessageContentPart#getToolCall()} and
     * {@link ChatMessageContentPart#getToolResponse()}.
     * <p>
     * Leave it null for models that don't need it; it is then omitted from the
     * request body entirely.
     */
    private Boolean includeServerSideToolInvocations;

    /**
     * Shorthand for the one setting gemini-4-argon requires when mixing
     * function declarations with built-in tools.
     */
    public static ToolConfig includeServerSideToolInvocations() {
        return ToolConfig.builder().includeServerSideToolInvocations(Boolean.TRUE).build();
    }
}
