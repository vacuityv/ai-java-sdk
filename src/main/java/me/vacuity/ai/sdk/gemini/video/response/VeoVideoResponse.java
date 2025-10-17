package me.vacuity.ai.sdk.gemini.video.response;

import lombok.Data;
import me.vacuity.ai.sdk.gemini.video.entity.VeoVideo;

import java.util.List;

/**
 * @description: Response from Gemini Veo video generation API (long-running operation)
 * @author: vacuity
 * @create: 2025-10-16
 **/

@Data
public class VeoVideoResponse {

    /**
     * Operation name/identifier
     */
    private String name;

    /**
     * Indicates if the operation is complete
     */
    private Boolean done;

    /**
     * Response data when the operation completes successfully
     */
    private Response response;

    @Data
    public static class Response {

        private Integer raiMediaFilteredCount;

        private String raiMediaFilteredReasons;

        private GenerateVideoResponse generateVideoResponse;
    }

    @Data
    public static class GenerateVideoResponse {

        private List<GeneratedSample> generatedSamples;

        private String raiMediaFilteredReason;

        private Integer raiMediaFilteredCount;
    }

    @Data
    public static class GeneratedSample {

        private VeoVideo video;
    }
}
