package me.vacuity.ai.sdk.openai.assistant.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.error.ChatResponseError;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 11:06
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantStreamResponse {

    private String event;
    
    private Class dataClass;

    private Thread thread;

    private Run run;
    
    private RunStep runStep;
    
    private RunStepDelta runStepDelta;
    
    private AssistantMessage message;
    
    private AssistantMessageDelta messageDelta;
    
    private ChatResponseError.ChatResponseErrorDetails error;
}
