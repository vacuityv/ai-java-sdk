package me.vacuity.ai.sdk.openai.image.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.image.entity.ImageUsage;

/**
 * One event from a streaming image generation or edit.
 * <p>
 * The four event types share the same shape, so this covers all of them; check
 * {@link #getType()} against
 * {@link me.vacuity.ai.sdk.openai.image.constant.ImageStreamEventConstant}.
 * Partial events carry {@code partialImageIndex}; completed events carry
 * {@code usage}.
 *
 * @author: vacuity
 * @create: 2026-09-10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageStreamEvent {

    /**
     * One of the constants in ImageStreamEventConstant.
     */
    private String type;

    /**
     * Base64-encoded image data, ready to render.
     */
    @JsonProperty("b64_json")
    private String b64Json;

    /**
     * 0-based index of this partial image. Only on partial_image events.
     */
    @JsonProperty("partial_image_index")
    private Integer partialImageIndex;

    /**
     * Unix timestamp of the event.
     */
    @JsonProperty("created_at")
    private Integer createdAt;

    /**
     * "transparent" / "opaque" / "auto".
     */
    private String background;

    /**
     * "png" / "webp" / "jpeg".
     */
    @JsonProperty("output_format")
    private String outputFormat;

    /**
     * "low" / "medium" / "high" / "xhigh" / "max" / "auto".
     */
    private String quality;

    /**
     * "WIDTHxHEIGHT", or "auto".
     */
    private String size;

    /**
     * Token usage. Only on completed events.
     */
    private ImageUsage usage;

    /**
     * Whether this is a final (completed) event rather than a partial one.
     */
    public boolean isCompleted() {
        return "image_generation.completed".equals(type) || "image_edit.completed".equals(type);
    }
}
