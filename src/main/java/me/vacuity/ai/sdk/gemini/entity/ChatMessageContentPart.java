package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:27
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageContentPart {

    private String text;

    @JsonProperty("inline_data")
    private InlineData inlineData;
    
    private ChatFunctionCall functionCall;

    private FunctionResponse functionResponse;

    private ExecutableCode executableCode;

    private CodeExecutionResult codeExecutionResult;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InlineData {

        @JsonProperty("mime_type")
        private String mimeType;

        private String data;
    }
}
