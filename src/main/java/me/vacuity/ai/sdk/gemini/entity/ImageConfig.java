package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Image generation settings, for models that return an IMAGE modality.
 * <p>
 * Values are not validated here — an unknown aspect ratio is rejected by the
 * server, which names what it accepts
 * ({@code generation_config.image_config.aspect_ratio: ...}).
 *
 * @author vacuity
 * @since 2.6.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageConfig {

    /** For example "16:9" or "1:1". */
    private String aspectRatio;

    /** For example "1K" or "2K". */
    private String imageSize;
}
