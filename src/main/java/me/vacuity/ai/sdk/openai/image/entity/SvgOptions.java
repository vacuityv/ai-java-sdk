package me.vacuity.ai.sdk.openai.image.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Options for SVG output, required when outputFormat is "svg".
 * <p>
 * Omitting it on an SVG request returns a 400 with
 * {@code missing_required_parameter} on {@code svg.model}.
 *
 * @author: vacuity
 * @create: 2026-11-05
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SvgOptions {

    /**
     * The model that authors the SVG, e.g. "gpt-6.1-terra". Billed separately
     * at that model's rates — see {@link SvgUsage}.
     */
    private String model;

    /**
     * Reasoning settings for the SVG author model. Optional; omit to use that
     * model's own default.
     *
     * @since 2.2.0
     */
    private SvgReasoning reasoning;

    public static SvgOptions model(String model) {
        return SvgOptions.builder().model(model).build();
    }

    /**
     * Shorthand for an authoring model plus a reasoning effort.
     *
     * @since 2.2.0
     */
    public static SvgOptions of(String model, String effort) {
        return SvgOptions.builder()
                .model(model)
                .reasoning(SvgReasoning.effort(effort))
                .build();
    }
}
