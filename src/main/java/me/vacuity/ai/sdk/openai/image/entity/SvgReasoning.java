package me.vacuity.ai.sdk.openai.image.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reasoning settings for the model that authors an SVG, independent of the
 * image {@code complexity} that produces the reference image.
 *
 * @author vacuity
 * @since 2.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SvgReasoning {

    /**
     * How much the SVG author model reasons: "none", "minimal", "low",
     * "medium", "high", "xhigh" or "max". Omit to use the author model's own
     * default; which efforts are accepted depends on that model.
     * <p>
     * It moves real money: on {@code gpt-6.1-terra} the same prompt reported
     * 1226 then 501 reasoning tokens with the effort unset, and 57 with
     * {@code "low"}.
     */
    private String effort;

    /**
     * Shorthand for {@code SvgReasoning.builder().effort(effort).build()}.
     */
    public static SvgReasoning effort(String effort) {
        return SvgReasoning.builder().effort(effort).build();
    }
}
