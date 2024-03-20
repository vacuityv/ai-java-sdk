package me.vacuity.ai.sdk.openai.assistant.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import me.vacuity.ai.sdk.openai.entity.ChatTool;

import java.util.List;
import java.util.Map;

/**
 * @description: assistant object
 * @author: vacuity
 * @create: 2024-03-19 17:37
 **/


@Data
@Builder
public class Assistant {

    private String id;
    
    private String object;

    @JsonProperty("created_at")
    private Integer createdAt;
    
    private String name;
    
    private String description;
    
    private String model;
    
    private String instructions;

    private List<ChatTool> tools;

    @JsonProperty("file_ids")
    private List<String> fileIds;

    private Map<String, Object> metadata;
}
