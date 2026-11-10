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
    
    /**
     * "transparent", "opaque" or "auto" (the default).
     * <p>
     * A transparent background needs {@code outputFormat} "png" or "webp";
     * "jpeg" is rejected.
     */
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
     * Neither GIF nor SVG can be combined with {@code stream} /
     * {@code partialImages} / {@code outputCompression}, and SVG additionally
     * requires {@code n} = 1 and an {@link #svg} block. Both also work on
     * edits as of November 2026 — SVG edits were rejected before that, so
     * anything written against the older behaviour is out of date.
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
     * Computation spent planning the image, independently of rendering
     * {@code quality}: "low", "medium" or "high".
     * <p>
     * Omit it to use the image model's own configured default — there is no
     * fixed default to rely on. For SVG output this controls the reference
     * image, not the SVG author's reasoning, which is
     * {@code svg.reasoning.effort}.
     */
    private String complexity;

    /**
     * Number of GIF frames, 2-16, upstream default 9.
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
     * Requested delay per GIF frame in milliseconds, minimum 20, upstream
     * default 200.
     * <p>
     * There is no upper bound on the parameter: the encoder rounds to the
     * nearest 10ms and caps the encoded delay at 655350ms per frame, and 5000
     * is accepted as sent.
     */
    @JsonProperty("frame_duration_ms")
    private Integer frameDurationMs;

    /**
     * SVG authoring options: the model that writes the SVG, and optionally how
     * hard it reasons.
     * <p>
     * {@code svg.model} is required whenever {@code outputFormat} is "svg" —
     * the server answers {@code missing_required_parameter} otherwise — and
     * sending this block with any other output format is rejected too. SVG
     * output additionally requires {@code n} = 1 and rules out
     * {@code stream} / {@code partialImages} / {@code outputCompression}.
     * <p>
     * The top-level {@code model} still selects the image model that creates
     * the reference image; {@code svg.model} is billed separately, reported as
     * {@code svgUsage} on the response.
     */
    private SvgOptions svg;

    /**
     * Stable per-end-user identifier used for abuse monitoring, at most 128
     * characters, echoed back on the response.
     * <p>
     * This replaces {@code user}, which is deprecated. A non-empty value here
     * takes precedence; {@code user} is only consulted when this is omitted,
     * null or empty. Sending both is accepted.
     */
    @JsonProperty("safety_identifier")
    private String safetyIdentifier;
}
