package me.vacuity.ai.sdk.openai.image.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An image the edit endpoint should read, named rather than uploaded.
 * <p>
 * Supply exactly one of the two: a File API id, or a URL. This is what the
 * JSON form of {@code /v1/images/edits} takes for its {@code images} entries
 * and for {@code mask} and {@code markup}, and it is the only way to reference
 * an already-uploaded file — the multipart form can only send bytes.
 *
 * @author vacuity
 * @since 2.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageReference {

    /**
     * The File API id of an uploaded image, as returned by the files endpoint
     * (for example {@code file-NNWydtXyDw54oT85NqjLNk}).
     */
    @JsonProperty("file_id")
    private String fileId;

    /**
     * A fully qualified URL, or a base64 data URL such as
     * {@code data:image/png;base64,...}. At most 20MB once encoded.
     */
    @JsonProperty("image_url")
    private String imageUrl;

    /** References an uploaded file by its File API id. */
    public static ImageReference fileId(String fileId) {
        return ImageReference.builder().fileId(fileId).build();
    }

    /** References an image by URL, including a base64 data URL. */
    public static ImageReference imageUrl(String imageUrl) {
        return ImageReference.builder().imageUrl(imageUrl).build();
    }
}
