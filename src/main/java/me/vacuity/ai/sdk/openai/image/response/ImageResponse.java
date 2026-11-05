package me.vacuity.ai.sdk.openai.image.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.image.entity.ImageData;
import me.vacuity.ai.sdk.openai.image.entity.ImageUsage;
import me.vacuity.ai.sdk.openai.image.entity.SvgUsage;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-04-26 18:20
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {

    private Integer created;

    private List<ImageData> data;
    
    private ImageUsage usage;

    /**
     * Tokens billed for authoring the SVG, present only on SVG output.
     * <p>
     * Separate from {@link #usage} because it is charged against the authoring
     * model named in {@code svg.model} at that model's own rates — a different
     * model from the one in the request.
     */
    @JsonProperty("svg_usage")
    private SvgUsage svgUsage;

    /**
     * The settings the image was produced with, echoed by gpt-image-2.6.
     * Useful when a request left them unset and took the server defaults.
     */
    private String background;

    @JsonProperty("output_format")
    private String outputFormat;

    private String quality;

    private String size;

    /**
     * The safety identifier the request carried, echoed back; null when none
     * was sent.
     */
    @JsonProperty("safety_identifier")
    private String safetyIdentifier;
}
