package me.vacuity.ai.sdk.gemini.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-02-22 14:28
 **/


@Data
@Builder
@AllArgsConstructor
public class ChatMessage {

    private String role;

    private List<ChatMessageContentPart> parts;

    public ChatMessage(String role, String content) {
        this.role = role;
        ChatMessageContentPart chatMessageContent = ChatMessageContentPart.builder().text(content).build();
        List<ChatMessageContentPart> parts = Arrays.asList(chatMessageContent);
        this.parts = parts;
    }

    public ChatMessage(String content) {
        ChatMessageContentPart chatMessageContent = ChatMessageContentPart.builder().text(content).build();
        List<ChatMessageContentPart> parts = Arrays.asList(chatMessageContent);
        this.parts = parts;
    }
}
