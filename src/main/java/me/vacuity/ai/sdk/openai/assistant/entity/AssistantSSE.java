package me.vacuity.ai.sdk.openai.assistant.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple Server Sent Event representation
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantSSE {

    private String event;

    private String data;
}