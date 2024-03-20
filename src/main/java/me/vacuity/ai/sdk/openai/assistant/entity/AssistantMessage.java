package me.vacuity.ai.sdk.openai.assistant.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 09:14
 **/

@Data
@Builder
public class AssistantMessage {

    private String id;
    
    private String object;
    
    @JsonProperty("created_at")
    private Integer createdAt;
    
    @JsonProperty("thread_id")
    private String threadId;
    
    private String status;
    
    @JsonProperty("incomplete_details")
    private IncompleteDetails incompleteDetails;

    @JsonProperty("completed_at")
    private Integer completedAt;

    @JsonProperty("incomplete_at")
    private Integer incompleteAt;

    private String role;

    private List<AssistantMessageContent> content;
    
    @JsonProperty("assistant_id")
    private String assistantId;

    @JsonProperty("run_id")
    private String runId;

    @JsonProperty("file_ids")
    private List<String> fileIds;

    private Map<String, Object> metadata;


    @Data
    @Builder
    public static class IncompleteDetails {
        private String reason;
    }
}
