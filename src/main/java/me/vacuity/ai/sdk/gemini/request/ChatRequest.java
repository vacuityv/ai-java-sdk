package me.vacuity.ai.sdk.gemini.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.gemini.entity.ChatMessage;
import me.vacuity.ai.sdk.gemini.entity.Tool;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:12
 **/


@Data
@Builder
public class ChatRequest {

    // default model: gemini-pro
    @Builder.Default
    @JsonIgnore
    private String model = "gemini-pro";

    private List<ChatMessage> contents;

    private List<SafetySetting> safetySettings;

    private GenerationConfig generationConfig;
    
    private ChatMessage systemInstruction;

    private List<Tool> tools;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SafetySetting {

        private String category;

        private String threshold;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerationConfig {

        private List<String> stopSequences;

        private float temperature;

        private Integer maxOutputTokens;

        private float topP;

        private float topK;
    }
}
