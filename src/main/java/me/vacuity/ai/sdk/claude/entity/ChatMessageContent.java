package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
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

    @JsonProperty("tool_use_id")
    private String toolUseId;

    private String content;
    
    // only in function response
    private String id;
    
    private String name;

    private JsonNode input;
    
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
