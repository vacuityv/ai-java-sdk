package me.vacuity.ai.sdk.openai.assistant.request;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 09:40
 **/

@Data
@Builder
public class ModifyAssistantMessageRequest {

    Map<String, String> metadata;
}
