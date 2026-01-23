package me.vacuity.ai.sdk.openai.responses.request;

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
public class ResponseReasoning {

    /**
     * The effort level for reasoning.
     * Possible values: "low", "medium", "high".
     */
    private String effort;

    /**
     * Whether to include a summary of reasoning in the response.
     */
    private String summary;

    /**
     * Deprecated: Use effort instead.
     */
    @JsonProperty("reasoning_effort")
    private String reasoningEffort;
}
