package me.vacuity.ai.sdk.openai.responses.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration options for reasoning models.
 *
 * @author: vacuity
 * @create: 2025-01-22
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseReasoning {

    /**
     * Constrains effort on reasoning.
     * Possible values: "none", "minimal", "low", "medium", "high", "xhigh", "max".
     * Support varies by model.
     */
    private String effort;

    /**
     * A summary of the reasoning performed by the model.
     * Possible values: "auto", "concise", "detailed".
     */
    private String summary;

    /**
     * Controls which reasoning items are rendered back to the model on later turns.
     * Possible values: "auto", "current_turn", "all_turns".
     */
    private String context;

    /**
     * Controls the reasoning execution mode for the request.
     * Possible values: "standard", "pro".
     */
    private String mode;

    /**
     * A summary of the reasoning performed by the model.
     *
     * @deprecated Use {@link #summary} instead.
     */
    @Deprecated
    @JsonProperty("generate_summary")
    private String generateSummary;

    /**
     * Not part of the Responses API reasoning object — `reasoning_effort` is a
     * Chat Completions top-level parameter. Kept only for source compatibility and
     * no longer serialized; setting it has no effect.
     *
     * @deprecated Use {@link #effort} instead.
     */
    @Deprecated
    @JsonIgnore
    private String reasoningEffort;
}
