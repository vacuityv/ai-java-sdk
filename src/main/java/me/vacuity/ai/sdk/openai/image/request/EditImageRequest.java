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
}
