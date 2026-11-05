package me.vacuity.ai.sdk.openai.image.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token usage of the model that authored the SVG. Only returned when
 * outputFormat is "svg".
 * <p>
 * This is billed at the authoring model's own rates, which differ from the
 * image model's, so {@link #getModel()} matters for cost accounting — it is
 * not the same model as the one named in the request.
 *
 * @author: vacuity
 * @create: 2026-11-05
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SvgUsage {

    /**
     * The authoring model, e.g. "gpt-6.1-terra".
     */
    private String model;

    @JsonProperty("input_tokens")
    private Integer inputTokens;

    @JsonProperty("output_tokens")
    private Integer outputTokens;

    @JsonProperty("reasoning_tokens")
    private Integer reasoningTokens;

    @JsonProperty("total_tokens")
    private Integer totalTokens;
}
