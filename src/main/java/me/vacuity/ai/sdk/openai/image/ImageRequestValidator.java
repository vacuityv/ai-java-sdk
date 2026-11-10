package me.vacuity.ai.sdk.openai.image;

import me.vacuity.ai.sdk.openai.image.request.CreateImageRequest;
import me.vacuity.ai.sdk.openai.image.request.EditImageRequest;

/**
 * The few request shapes the SDK refuses locally.
 * <p>
 * This is deliberately thin. The API enforces a long list of format
 * restrictions — gif rules out streaming, partial images, masks and
 * output_compression; svg rules out those plus n &gt; 1; a transparent
 * background rules out jpeg — and mirroring all of them here was tried and
 * dropped: every mirrored rule is a rule the SDK would keep enforcing after
 * upstream relaxed it, and it costs the live tests their ability to notice
 * that happening. Those are left to the server, and
 * {@code OpenaiImageLiveSmokeTest} pins what it currently answers.
 * <p>
 * What stays are the two cases where the server's answer would not point at
 * the real mistake, plus the fields a given transport physically cannot carry
 * — a field that is set and silently never sent is the failure mode that hid
 * {@code moderation} from 1.9.17 through 1.11.0.
 *
 * @author vacuity
 * @since 2.2.0
 */
public final class ImageRequestValidator {

    private ImageRequestValidator() {
    }

    /**
     * @throws IllegalArgumentException if svg output is requested without an
     *                                  authoring model
     */
    public static void validate(CreateImageRequest request) {
        if (request == null) {
            return;
        }
        // The server answers missing_required_parameter on svg.model, which is
        // clear enough — but it costs a round trip to learn that a nested
        // object was left out, so this one is worth catching.
        requireSvgModel(request.getOutputFormat(), request.getSvg());
    }

    /**
     * SVG output needs an authoring model on both endpoints. Edits gained SVG
     * support in November 2026 and inherited the same requirement.
     */
    private static void requireSvgModel(
            String outputFormat,
            me.vacuity.ai.sdk.openai.image.entity.SvgOptions svg) {
        if ("svg".equals(outputFormat) && (svg == null || isBlank(svg.getModel()))) {
            throw new IllegalArgumentException(
                    "svg output requires svg.model — the model that authors the SVG, "
                            + "e.g. .svg(SvgOptions.model(\"gpt-6.1-terra\"))");
        }
    }

    /**
     * Validates the multipart form of an edit, where the mask arrives as a
     * method argument.
     *
     * @throws IllegalArgumentException if a mask and a markup layer are both
     *                                  given, or if the request carries
     *                                  reference fields this transport cannot
     *                                  send
     */
    public static void validate(EditImageRequest request, java.io.File mask) {
        if (request == null) {
            return;
        }
        // "The 'mask' and 'markup' parameters cannot be used together." Worth
        // catching because the two arrive by different routes — one a method
        // argument, one a request field — so the clash is easy to create by
        // accident.
        if (request.getMarkup() != null && mask != null) {
            throw new IllegalArgumentException(
                    "markup and mask cannot be sent together on an image edit");
        }
        requireSvgModel(request.getOutputFormat(), request.getSvg());
        // These can only go out as JSON. Sending them to the multipart form
        // would drop them without a word.
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            throw new IllegalArgumentException(
                    "images references cannot be sent as multipart — call "
                            + "editImage(request) for the JSON form, or pass the files instead");
        }
        rejectUnsendableReferences(request);
    }

    /**
     * Validates the JSON form of an edit, where every input is named.
     *
     * @throws IllegalArgumentException if no input image is named, too many
     *                                  are, both a mask and a markup layer
     *                                  are, or a {@code java.io.File} is set
     *                                  that this transport cannot send
     */
    public static void validateByReference(EditImageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (request.getImages() == null || request.getImages().isEmpty()) {
            throw new IllegalArgumentException(
                    "editImage(request) needs at least one entry in images — use "
                            + "ImageReference.fileId(..) or ImageReference.imageUrl(..), or call "
                            + "an overload that takes files");
        }
        if (request.getImages().size() > 16) {
            throw new IllegalArgumentException(
                    "at most 16 input images are supported, got " + request.getImages().size());
        }
        if (request.getMarkupReference() != null && request.getMaskReference() != null) {
            throw new IllegalArgumentException(
                    "markup and mask cannot be sent together on an image edit");
        }
        requireSvgModel(request.getOutputFormat(), request.getSvg());
        // The JSON body carries no file uploads, so a File set here is lost.
        if (request.getMarkup() != null) {
            throw new IllegalArgumentException(
                    "the markup file cannot be sent as JSON — use markupReference with "
                            + "ImageReference.imageUrl(..) or .fileId(..)");
        }
    }

    private static void rejectUnsendableReferences(EditImageRequest request) {
        if (request.getMaskReference() != null) {
            throw new IllegalArgumentException(
                    "maskReference cannot be sent as multipart — pass the mask file to this "
                            + "overload, or call editImage(request) for the JSON form");
        }
        if (request.getMarkupReference() != null) {
            throw new IllegalArgumentException(
                    "markupReference cannot be sent as multipart — set the markup file "
                            + "instead, or call editImage(request) for the JSON form");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
