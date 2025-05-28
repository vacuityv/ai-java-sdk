package me.vacuity.ai.sdk.gemini.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.gemini.entity.ChatMessage;
import me.vacuity.ai.sdk.gemini.entity.Tool;
import me.vacuity.ai.sdk.gemini.enums.MediaResolution;
import me.vacuity.ai.sdk.gemini.enums.Modality;

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
        
        private String responseMimeType;
        
        private List<Modality> responseModalities;
        
        private Integer candidateCount;

        private Float temperature;

        private Integer maxOutputTokens;

        private Float topP;

        private Float topK;
        
        private Integer seed;
        
        private Float presencePenalty;
        
        private Float frequencyPenalty;
        
        private Boolean responseLogprobs;
        
        private Integer logprobs;
        
        private Boolean enableEnhancedCivicAnswers;
        
        private MediaResolution mediaResolution;
        
        private ThinkingConfig thinkingConfig;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThinkingConfig {
        
        private Boolean includeThoughts;
        
        private Integer thinkingBudget;
    }
}
