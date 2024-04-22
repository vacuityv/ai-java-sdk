package me.vacuity.ai.sdk.claude.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

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

    private Object content;

    public ChatMessage(String role, String msg) {
        this.role = role;
        this.content = msg;
    }

    public ChatMessage(String role, List<ChatMessageContent> contents) {
        this.role = role;
        this.content = contents;
    }

}
