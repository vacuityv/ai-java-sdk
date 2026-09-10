package me.vacuity.ai.sdk.openai.image.constant;

/**
 * Event types emitted while streaming image generation or editing.
 *
 * @author: vacuity
 * @create: 2026-09-10
 **/
public class ImageStreamEventConstant {

    /** A partial image during generation. Carries partialImageIndex. */
    public static final String GENERATION_PARTIAL_IMAGE = "image_generation.partial_image";

    /** The finished image from generation. Carries usage. */
    public static final String GENERATION_COMPLETED = "image_generation.completed";

    /** A partial image during editing. Carries partialImageIndex. */
    public static final String EDIT_PARTIAL_IMAGE = "image_edit.partial_image";

    /** The finished image from editing. Carries usage. */
    public static final String EDIT_COMPLETED = "image_edit.completed";
}
