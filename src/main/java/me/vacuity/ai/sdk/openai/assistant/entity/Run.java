package me.vacuity.ai.sdk.openai.assistant.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import me.vacuity.ai.sdk.openai.assistant.entity.inner.LastError;
import me.vacuity.ai.sdk.openai.entity.ChatFunctionCall;
import me.vacuity.ai.sdk.openai.entity.ChatTool;
import me.vacuity.ai.sdk.openai.response.ChatResponse;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 09:42
 **/


@Data
@Builder
public class Run {

    private String id;

    private String object;

    @JsonProperty("created_at")
    private Integer createdAt;

    @JsonProperty("thread_id")
    private String threadId;

    @JsonProperty("assistant_id")
    private String assistantId;

    private String status;

    @JsonProperty("required_action")
    private RequiredAction requiredAction;

    @JsonProperty("last_error")
    private LastError lastError;

    @JsonProperty("expires_at")
    private Integer expiresAt;

    @JsonProperty("started_at")
    private Integer startedAt;

    @JsonProperty("cancelled_at")
    private Integer cancelledAt;

    @JsonProperty("failed_at")
    private Integer failedAt;

    @JsonProperty("completed_at")
    private Integer completedAt;

    private String model;

    private String instructions;

    private List<ChatTool> tools;

    @JsonProperty("file_ids")
    private List<String> fileIds;

    private Map<String, Object> metadata;

    private ChatResponse.Usage usage;


    @Data
    @Builder
    public static class RequiredAction {

        private String type;

        @JsonProperty("submit_tool_outputs")
        private SubmitToolOutputs submitToolOutputs;


        @Data
        @Builder
        public static class SubmitToolOutputs {

            @JsonProperty("tool_calls")
            private List<ChatFunctionCall> toolCalls;
        }
    }

}
