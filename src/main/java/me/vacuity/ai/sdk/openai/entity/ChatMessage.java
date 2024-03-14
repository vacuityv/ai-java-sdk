package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-13 14:25
 **/


@Data
@Builder
@AllArgsConstructor
public class ChatMessage {

    private String role;

    private String name;

    private Object content;

    @JsonProperty("tool_call_id")
    private String toolCallId;

    private List<ChatFunctionCall> toolCalls;


    public ChatMessage() {
    }

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public ChatMessage(String role, String content, String toolCallId) {
        this.role = role;
        this.content = content;
        this.toolCallId = toolCallId;
    }

    public ChatMessage(String role, List<ChatMessageContent> contentList) {
        this.role = role;
        this.content = contentList;
    }
}
