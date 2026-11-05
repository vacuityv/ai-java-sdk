package me.vacuity.ai.sdk.openai.image.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.vacuity.ai.sdk.openai.image.entity.SvgOptions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 18:16
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateImageRequest {

    private String prompt;
    
    private String background;

    private String model;
    
    private String moderation;

    private Integer n;

    @JsonProperty("output_compression")
    private Integer outputCompression;
    
    /**
     * Output container: "png", "jpeg", "webp", or, on gpt-image-2.6, "gif" and
     * "svg".
     * <p>
     * GIF and SVG are generations-only and cannot be combined with
     * {@code stream} / {@code partialImages} / {@code outputCompression};
     * SVG additionally requires {@code n} = 1 and an {@link #svg} block.
     */
    @JsonProperty("output_format")
    private String outputFormat;

    private String quality;

    @JsonProperty("response_format")
    private String responseFormat;

    private String size;

    private String style;

    private String user;

    /**
     * Stream partial images as the final image is produced. Use the
     * streaming client methods rather than setting this by hand.
     */
    private Boolean stream;

    /**
     * How many partial images to emit while streaming (0-3).
     */
    @JsonProperty("partial_images")
    private Integer partialImages;

    /**
     * How much detail to put into the image: "low", "medium" or "high".
     * Upstream default is "high". gpt-image-2.6 and later.
     */
    private String complexity;

    /**
     * Number of GIF frames, 2-64, upstream default 9.
     * <p>
     * Frames are rendered as one sprite sheet before being assembled, so
     * {@code frameCount} and {@code size} are not independent: the sheet's
     * longest edge must stay within 3840px. 4 frames at 1024x1024 is rejected
     * ("No supported sheet fits 4 GIF frames at size '1024x1024'"), while the
     * same 4 frames at 512x512 are fine. Drop the size, not the frame count,
     * when that error comes back.
     */
    @JsonProperty("frame_count")
    private Integer frameCount;

    /**
     * How long each GIF frame is shown, 20-2000ms, upstream default 200.
     */
    @JsonProperty("frame_duration_ms")
    private Integer frameDurationMs;

    /**
     * The model that authors the SVG. Required whenever
     * {@code outputFormat} is "svg" — the server answers
     * {@code missing_required_parameter} on {@code svg.model} otherwise.
     */
    private SvgOptions svg;

    /**
     * Stable per-end-user identifier used for abuse monitoring, echoed back on
     * the response. The newer name for what {@code user} carried; both are
     * still accepted, and sending both together is allowed.
     */
    @JsonProperty("safety_identifier")
    private String safetyIdentifier;
}
