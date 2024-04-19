package me.vacuity.ai.sdk.openai.assistant.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.assistant.entity.inner.TruncationStrategy;
import me.vacuity.ai.sdk.openai.entity.ChatTool;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 10:35
 **/

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RunRequest {

    @JsonProperty("assistant_id")
    private String assistantId;

    private String model;

    private String instructions;

    @JsonProperty("additional_instructions")
    private String additionalInstructions;

    @JsonProperty("additional_messages")
    private List<AssistantMessageRequest> additionalMessages;

    private List<ChatTool> tools;

    private Map<String, Object> metadata;

    private Boolean stream;

    private Float temperature;

    @JsonProperty("top_p")
    private Double topP;

    @JsonProperty("response_format")
    private Object responseFormat;

    @JsonProperty("max_prompt_tokens")
    private Integer maxPromptTokens;

    @JsonProperty("max_completion_tokens")
    private Integer maxCompletionTokens;

    @JsonProperty("truncation_strategy")
    private TruncationStrategy truncationStrategy;

    @JsonProperty("tool_choice")
    private Object toolChoice;
}
