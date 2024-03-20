package me.vacuity.ai.sdk.openai.assistant.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.assistant.entity.inner.LastError;
import me.vacuity.ai.sdk.openai.assistant.entity.inner.RunStepDetails;
import me.vacuity.ai.sdk.openai.response.ChatResponse;

import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 09:52
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RunStep {

    private String id;

    private String object;

    @JsonProperty("created_at")
    private Integer createdAt;

    @JsonProperty("thread_id")
    private String threadId;

    @JsonProperty("assistant_id")
    private String assistantId;
    
    @JsonProperty("run_id")
    private String runId;

    private String type;
    
    private String status;

    @JsonProperty("step_details")
    private RunStepDetails stepDetails;

    @JsonProperty("last_error")
    private LastError lastError;

    @JsonProperty("expired_at")
    private Integer expiredAt;

    @JsonProperty("cancelled_at")
    private Integer cancelledAt;

    @JsonProperty("failed_at")
    private Integer failedAt;

    @JsonProperty("completed_at")
    private Integer completedAt;

    private Map<String, Object> metadata;

    private ChatResponse.Usage usage;
}
