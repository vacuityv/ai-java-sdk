package me.vacuity.ai.sdk.openai.responses.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Content item for message input.
 * Can be text, image, or file content.
 *
 * @author: vacuity
 * @create: 2025-01-22
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseInputContent {

    /**
     * The type of the content.
     * Can be "input_text", "input_image", "input_file", etc.
     */
    private String type;

    /**
     * The text content (for input_text type).
     */
    private String text;

    /**
     * The URL of the image (for input_image type).
     */
    @JsonProperty("image_url")
    private String imageUrl;

    /**
     * The base64-encoded image data (for input_image type).
     */
    private String image;

    /**
     * The file ID (for input_file type).
     */
    @JsonProperty("file_id")
    private String fileId;

    /**
     * The filename (for input_file type).
     */
    private String filename;

    /**
     * The base64-encoded file data (for input_file type).
     */
    @JsonProperty("file_data")
    private String fileData;

    /**
     * Detail level for images. Can be "low", "high", or "auto".
     */
    private String detail;

    /**
     * Convenient static method to create a text content.
     */
    public static ResponseInputContent text(String text) {
        return ResponseInputContent.builder()
                .type("input_text")
                .text(text)
                .build();
    }

    /**
     * Convenient static method to create an image URL content.
     */
    public static ResponseInputContent imageUrl(String imageUrl) {
        return ResponseInputContent.builder()
                .type("input_image")
                .imageUrl(imageUrl)
                .build();
    }

    /**
     * Convenient static method to create an image URL content with detail level.
     */
    public static ResponseInputContent imageUrl(String imageUrl, String detail) {
        return ResponseInputContent.builder()
                .type("input_image")
                .imageUrl(imageUrl)
                .detail(detail)
                .build();
    }

    /**
     * Convenient static method to create a base64 image content.
     */
    public static ResponseInputContent imageBase64(String base64Data) {
        return ResponseInputContent.builder()
                .type("input_image")
                .image(base64Data)
                .build();
    }

    /**
     * Convenient static method to create a file content.
     */
    public static ResponseInputContent file(String fileId) {
        return ResponseInputContent.builder()
                .type("input_file")
                .fileId(fileId)
                .build();
    }
}
