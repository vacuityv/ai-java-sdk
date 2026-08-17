package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-13 14:25
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {

    private String role;

    private String name;

    private Object content;

    @JsonProperty("tool_call_id")
    private String toolCallId;

    private List<ChatFunctionCall> toolCalls;

    /**
     * deepseek thinking 模型（v4-pro 等）调用 tools 后，
     * 第二轮请求需要把第一轮模型返回的 reasoning_content 原样回灌；
     * 否则 API 会返回 "The reasoning_content in the thinking mode must be passed back to the API."。
     */
    @JsonProperty("reasoning_content")
    private String reasoningContent;


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
