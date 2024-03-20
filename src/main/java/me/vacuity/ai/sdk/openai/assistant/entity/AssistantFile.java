package me.vacuity.ai.sdk.openai.assistant.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @description: assistant file object
 * @author: vacuity
 * @create: 2024-03-19 17:37
 **/


@Data
@Builder
public class AssistantFile {

    private String id;

    private String object;

    @JsonProperty("created_at")
    private Integer createdAt;

    @JsonProperty("assistant_id")
    private String assistantId;
}
