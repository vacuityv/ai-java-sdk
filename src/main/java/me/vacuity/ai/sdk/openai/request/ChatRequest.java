package me.vacuity.ai.sdk.openai.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import me.vacuity.ai.sdk.openai.entity.ChatMessage;
import me.vacuity.ai.sdk.openai.entity.ChatTool;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-13 14:33
 **/

@Data
@Builder
public class ChatRequest {


    private List<ChatMessage> messages;

    private String model;

    @JsonProperty("frequency_penalty")
    private Float frequencyPenalty;

    @JsonProperty("logit_bias")
    private Map<String, Integer> logitBias;

    private Boolean logprobs;

    @JsonProperty("top_logprobs")
    private Integer topLogprobs;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    private Integer n;

    @JsonProperty("presence_penalty")
    private Float presencePenalty;

    @JsonProperty("response_format")
    private Map<String, Object> responseFormat;

    private Integer seed;

    private Object stop;

    private Boolean stream;

    private Float temperature;

    @JsonProperty("top_p")
    private Double topP;
    
    private List<ChatTool> tools;
    
    @JsonProperty("tool_choice")
    private Object toolChoice;
}
