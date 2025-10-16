package me.vacuity.ai.sdk.gemini.video.constant;

/**
 * @description: Veo video generation constants
 * @author: vacuity
 * @create: 2025-10-16
 **/

public class VeoVideoConstant {

    /**
     * Aspect ratio constants
     */
    public static class AspectRatio {
        public static final String RATIO_16_9 = "16:9";
        public static final String RATIO_9_16 = "9:16";
    }

    /**
     * Resolution constants (Veo 3 models only)
     */
    public static class Resolution {
        public static final String RES_720P = "720p";
        public static final String RES_1080P = "1080p";
    }

    /**
     * Reference image type constants
     */
    public static class ReferenceImageType {
        public static final String ASSET = "REFERENCE_IMAGE_TYPE_ASSET";
        public static final String STYLE = "REFERENCE_IMAGE_TYPE_STYLE";
    }

    /**
     * Duration constants
     */
    public static class Duration {
        public static final int MIN_SECONDS = 4;
        public static final int MAX_SECONDS = 8;
    }

    /**
     * Sample count constants
     */
    public static class SampleCount {
        public static final int MIN_COUNT = 1;
        public static final int MAX_COUNT = 4;
    }
}
