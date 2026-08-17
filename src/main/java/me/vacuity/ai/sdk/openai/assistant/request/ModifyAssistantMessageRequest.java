package me.vacuity.ai.sdk.openai.assistant.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 09:40
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModifyAssistantMessageRequest {

    Map<String, String> metadata;
}
