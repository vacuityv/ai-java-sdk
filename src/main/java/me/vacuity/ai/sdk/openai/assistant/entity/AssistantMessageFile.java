package me.vacuity.ai.sdk.openai.assistant.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 09:34
 **/


@Data
@Builder
public class AssistantMessageFile {

    private String id;
    
    private String object;
    
    @JsonProperty("created_at")
    private Integer createdAt;
    
    @JsonProperty("message_id")
    private String messageId;
}
