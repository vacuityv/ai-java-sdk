package me.vacuity.ai.sdk.openai.assistant.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import me.vacuity.ai.sdk.openai.entity.ChatTool;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-19 18:18
 **/


@Data
@Builder
public class ModifyAssistantRequest {

    private String model;

    private String name;

    private String description;

    private String instructions;

    private List<ChatTool> tools;

    @JsonProperty("file_ids")
    private List<String> fileIds;

    private Map<String, Object> metadata;
}
