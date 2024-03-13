package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-13 14:29
 **/


@Data
public class ChatMessageContent {

    private String type;

    private String text;

    @JsonProperty("image_url")
    private ImageUrl imageUrl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageUrl {

        // Either a URL of the image or the base64 encoded image data. If base64 data must begin with: "data:" + mimeType + ";base64,"
        private String url;

        private String detail;
    }
}
