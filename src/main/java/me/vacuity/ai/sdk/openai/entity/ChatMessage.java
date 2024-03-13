package me.vacuity.ai.sdk.openai.entity;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-13 14:25
 **/


@Data
public class ChatMessage {

    private String role;

    private String name;

    private Object content;

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public ChatMessage(String role, List<ChatMessageContent> contentList) {
        this.role = role;
        this.content = contentList;
    }

//    public String getStringContent() {
//        return (String) content;
//    }
}
