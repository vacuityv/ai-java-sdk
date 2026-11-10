package me.vacuity.ai.sdk.openai.image.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class EditImageRequest {

    private String prompt;

    private String model;

    private Integer n;

    private String quality;

    private String size;

    @JsonProperty("response_format")
    private String responseFormat;

    private String user;

    @JsonProperty("input_fidelity")
    private String inputFidelity;

    /**
     * "low" 放宽过滤；"auto" 为默认。仅 gpt-image 系列生效。
     */
    private String moderation;

    /**
     * 背景处理："transparent" / "opaque" / "auto"。
     * 透明背景要求 output_format 为 png 或 webp。仅 gpt-image 系列生效。
     */
    private String background;

    /**
     * 输出格式："png" / "jpeg" / "webp"。仅 gpt-image 系列生效。
     */
    @JsonProperty("output_format")
    private String outputFormat;

    /**
     * 输出压缩率 0-100，仅当 outputFormat 为 jpeg 或 webp 时有效。
     */
    @JsonProperty("output_compression")
    private Integer outputCompression;

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
     * Rendering effort: "low", "medium" or "high". Upstream defaults to "high"
     * when omitted.
     */
    private String complexity;

    /**
     * Optional annotation layer sent alongside the input image.
     * <p>
     * Must be a PNG matching the first input image's dimensions after
     * orientation and containing transparent pixels, and cannot be combined
     * with a mask. Carried on the request object rather than as another method
     * parameter, so the existing editImage overloads keep their signatures.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.io.File markup;

    /**
     * Stable per-end-user identifier used for abuse monitoring, at most 128
     * characters, echoed back on the response.
     * <p>
     * This replaces {@code user}, which is deprecated. A non-empty value here
     * takes precedence; {@code user} is only consulted when this is omitted,
     * null or empty.
     */
    @JsonProperty("safety_identifier")
    private String safetyIdentifier;

    /**
     * Number of GIF frames, 2-16, upstream default 9. Only sent with
     * {@code outputFormat} "gif".
     * <p>
     * Frames are rendered as one sprite sheet, so this and {@code size} are not
     * independent: the sheet's longest edge must stay within 3840px, and 16
     * frames at 1024x1024 is rejected. The sheet also decides the pixel budget,
     * which is why a GIF edit at 512x512 is accepted while a PNG edit at the
     * same size is "below the current minimum pixel budget".
     *
     * @since 2.2.0
     */
    @JsonProperty("frame_count")
    private Integer frameCount;

    /**
     * Requested delay per GIF frame in milliseconds, minimum 20, upstream
     * default 200. Only sent with {@code outputFormat} "gif".
     * <p>
     * There is no upper bound on the parameter: the encoder rounds to the
     * nearest 10ms and caps the encoded delay at 655350ms per frame, and 5000
     * is accepted as sent.
     *
     * @since 2.2.0
     */
    @JsonProperty("frame_duration_ms")
    private Integer frameDurationMs;

    /**
     * Input images named rather than uploaded, 1-16 of them.
     * <p>
     * Setting this selects the JSON form of the endpoint, which is the only
     * form that can reference a File API id. Use {@code editImage(request)} —
     * the overloads that take {@code java.io.File} send multipart and cannot
     * carry these.
     *
     * @since 2.2.0
     */
    private java.util.List<me.vacuity.ai.sdk.openai.image.entity.ImageReference> images;

    /**
     * The mask, named rather than uploaded. JSON form only; the multipart
     * overloads take the mask as a {@code java.io.File} argument instead.
     *
     * @since 2.2.0
     */
    @JsonProperty("mask")
    private me.vacuity.ai.sdk.openai.image.entity.ImageReference maskReference;

    /**
     * The markup layer, named rather than uploaded. JSON form only; the
     * multipart form uses the {@code markup} {@code java.io.File} field.
     * Still mutually exclusive with a mask.
     *
     * @since 2.2.0
     */
    @JsonProperty("markup")
    private me.vacuity.ai.sdk.openai.image.entity.ImageReference markupReference;

    /**
     * SVG authoring options. Required when {@code outputFormat} is "svg".
     * <p>
     * SVG edits were rejected outright until early November 2026 — the server
     * answered "Invalid value: 'svg'. Supported values are: 'png', 'webp', and
     * 'jpeg'." They now work on gpt-image-2.6, and the SDK's own live canary
     * is what caught the change. Like SVG generation they require
     * {@code svg.model} and {@code n} = 1, and rule out streaming; unlike it,
     * a mask is accepted.
     *
     * @since 2.2.1
     */
    private me.vacuity.ai.sdk.openai.image.entity.SvgOptions svg;
}
