package me.vacuity.ai.sdk.gemini.video.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @description: Request for creating videos with Gemini Veo API
 * @author: vacuity
 * @create: 2025-10-16
 **/

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VeoVideoRequest {

    /**
     * List of instances containing the video generation parameters
     */
    private List<Instance> instances;

    /**
     * Additional generation parameters
     */
    private Parameters parameters;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Instance {
        /**
         * Text prompt describing the desired video
         */
        private String prompt;

        /**
         * Optional input image for image-to-video generation
         */
        private Media image;

        private Media lastFrame;
        
        private Media video;

        /**
         * Optional reference images for style or asset guidance
         */
        private List<ReferenceImage> referenceImages;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Media {
        /**
         * Base64 encoded image bytes
         */
        @JsonProperty("bytesBase64Encoded")
        private String bytesBase64Encoded;

        @JsonProperty("gcsUri")
        private String gcsUri;

        /**
         * MIME type of the image (e.g., "image/jpeg", "image/png")
         */
        private String mimeType;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReferenceImage {
        /**
         * Reference image data
         */
        private Media image;

        /**
         * Type of reference: "REFERENCE_IMAGE_TYPE_ASSET" or "REFERENCE_IMAGE_TYPE_STYLE"
         */
        private String referenceType;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Parameters {

        /**
         * Aspect ratio: "16:9" or "9:16"
         */
        private String aspectRatio;
        
        /**
         * 'optimized' or 'lossless'
         */
        private String compressionQuality;
        
        /**
         * Duration of the generated video in seconds (4-8 seconds, varies by model)
         */
        private Integer durationSeconds;

        /**
         * 可选。使用 Gemini 优化提示。接受的值包括 true 或 false。默认值为 true。
         */
        private Boolean enhancePrompt;
        
        /**
         * Veo 3 模型必须具有此参数。为视频生成音频。接受的值包括 true 或 false。
         *
         * generateAudio 不受 veo-2.0-generate-001 或 veo-2.0-generate-exp 支持。
         */
        private Boolean generateAudio;

        private String negativePrompt;
        
        private String personGeneration;

        /**
         * Resolution: "720p" or "1080p" (Veo 3 models only)
         */
        private String resolution;

        /**
         * Number of video outputs to generate (1-4)
         */
        private Integer sampleCount;
        

        /**
         * Seed for deterministic video generation
         */
        private Integer seed;
        

        /**
         * 可选。用于存储输出视频的 Cloud Storage 存储桶 URI，格式为 gs://BUCKET_NAME/SUBDIRECTORY。如果未提供 Cloud Storage 存储桶，则回答中会返回以 base64 编码的视频字节。
         */
        private String storageUri;
    }
}
