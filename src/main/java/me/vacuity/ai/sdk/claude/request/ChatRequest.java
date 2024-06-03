package me.vacuity.ai.sdk.claude.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import me.vacuity.ai.sdk.claude.entity.ChatFunction;
import me.vacuity.ai.sdk.claude.entity.ChatMessage;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:12
 **/


@Data
@Builder
public class ChatRequest {

    private String model;

    private List<ChatMessage> messages;

    private String system;

    @NonNull
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    private Map<String, Object> metadata;

    @JsonProperty("stop_sequences")
    private List<String> stopSequences;

    private boolean stream;

    private float temperature;

    @JsonProperty("top_p")
    private float topP;

    @JsonProperty("top_k")
    private Integer topK;

    private List<ChatFunction> tools;
}
