package me.vacuity.ai.sdk.openai.assistant.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-19 18:24
 **/


@Data
@Builder
public class ThreadRequest {
    
    private List<AssistantMessageRequest> messages;

    private Map<String, Object> metadata;
}
