package me.vacuity.ai.sdk.openai.responses.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * An input item for the Responses API.
 * Can be a message, function call output, or other input types.
 *
 * @author: vacuity
 * @create: 2025-01-22
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseInputItem {

    /**
     * The type of the input item.
     * Can be "message", "function_call_output", "item_reference", etc.
     */
    private String type;

    /**
     * The role of the message. One of "user", "system", "developer", or "assistant".
     */
    private String role;

    /**
     * The content of the message. Can be a string or a list of content items.
     */
    private Object content;

    /**
     * The unique ID of the item (for item_reference type).
     */
    private String id;

    /**
     * The unique ID of the function tool call (for function_call_output type).
     */
    @JsonProperty("call_id")
    private String callId;

    /**
     * The output of the function call (for function_call_output type).
     */
    private Object output;

    /**
     * The status of the item.
     */
    private String status;

    /**
     * Convenient static method to create a user message.
     */
    public static ResponseInputItem userMessage(String content) {
        return ResponseInputItem.builder()
                .type("message")
                .role("user")
                .content(content)
                .build();
    }

    /**
     * Convenient static method to create a user message with content list.
     */
    public static ResponseInputItem userMessage(List<ResponseInputContent> contentList) {
        return ResponseInputItem.builder()
                .type("message")
                .role("user")
                .content(contentList)
                .build();
    }

    /**
     * Convenient static method to create a system message.
     */
    public static ResponseInputItem systemMessage(String content) {
        return ResponseInputItem.builder()
                .type("message")
                .role("system")
                .content(content)
                .build();
    }

    /**
     * Convenient static method to create a developer message.
     */
    public static ResponseInputItem developerMessage(String content) {
        return ResponseInputItem.builder()
                .type("message")
                .role("developer")
                .content(content)
                .build();
    }

    /**
     * Convenient static method to create a function call output.
     */
    public static ResponseInputItem functionCallOutput(String callId, String output) {
        return ResponseInputItem.builder()
                .type("function_call_output")
                .callId(callId)
                .output(output)
                .build();
    }
}
