package me.vacuity.ai.sdk.gemini.decision.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One block of the shared input every question is scored against.
 * <p>
 * The blocks are flat and {@code type} is <strong>required</strong>. The
 * official page lists each block's fields without it — describing an image
 * block as "{@code data} or {@code uri}, {@code mime_type}" — and a block sent
 * that way is rejected with
 * {@code The 'type' parameter is required at 'input[0]'}. Verified against the
 * live API on 2026-11-12.
 *
 * @author vacuity
 * @since 2.5.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DecisionInput {

    /**
     * "text", "image", "audio", "video" or "document". Required on every
     * block.
     */
    private String type;

    /** text blocks only. */
    private String text;

    /**
     * Inline base64 for a media block. Up to 100MB per request, 50MB for
     * PDFs. Supply this or {@link #uri}, not both.
     */
    private String data;

    /**
     * An external URL for a media block, up to 15MB, fetched by Google rather
     * than by this SDK — so it has to be reachable from their side, not only
     * from yours. An unreachable one answers
     * {@code Cannot fetch content from the provided URL}. YouTube links and
     * {@code gs://} URIs are not supported.
     */
    private String uri;

    /** Required on media blocks. Documents accept "application/pdf" only. */
    @JsonProperty("mime_type")
    private String mimeType;

    /**
     * "low", "medium", "high", or "ultra_high" for images. Optional; the page
     * states no default.
     */
    private String resolution;

    /** A text block. */
    public static DecisionInput text(String text) {
        return DecisionInput.builder().type("text").text(text).build();
    }

    /** An image from inline base64. */
    public static DecisionInput image(String base64Data, String mimeType) {
        return media("image", base64Data, mimeType);
    }

    /** An image from inline base64, at a given resolution. */
    public static DecisionInput image(String base64Data, String mimeType, String resolution) {
        return DecisionInput.builder().type("image")
                .data(base64Data).mimeType(mimeType).resolution(resolution).build();
    }

    /** Audio from inline base64. */
    public static DecisionInput audio(String base64Data, String mimeType) {
        return media("audio", base64Data, mimeType);
    }

    /** Video from inline base64. */
    public static DecisionInput video(String base64Data, String mimeType) {
        return media("video", base64Data, mimeType);
    }

    /**
     * A PDF from inline base64. Its tokens are reported under the "image"
     * modality, not a document one.
     */
    public static DecisionInput document(String base64Data) {
        return media("document", base64Data, "application/pdf");
    }

    /** Any media block referenced by URL instead of uploaded. */
    public static DecisionInput uri(String type, String uri, String mimeType) {
        return DecisionInput.builder().type(type).uri(uri).mimeType(mimeType).build();
    }

    private static DecisionInput media(String type, String base64Data, String mimeType) {
        return DecisionInput.builder().type(type).data(base64Data).mimeType(mimeType).build();
    }
}
