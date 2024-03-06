package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:27
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageContent {
    
    private String type;
    
    private String text;
    
    private ContentSource source;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentSource {
        private String type;

        @JsonProperty("media_type")
        private String mediaType;
        
        private String data;
    }
}
