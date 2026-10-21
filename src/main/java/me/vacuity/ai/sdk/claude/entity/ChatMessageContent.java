package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:27
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageContent {

    private String type;

    private String text;
    
    private String thinking;
    
    private String signature;

    private ContentSource source;

    @JsonProperty("tool_use_id")
    private String toolUseId;

    private Object content;

    // only in function response
    private String id;

    private String name;

    private JsonNode input;

    @JsonProperty("partial_json")
    private String partialJson;

    /**
     * Cache breakpoint. Everything rendered before this block
     * (tools -> system -> messages) is cached.
     */
    @JsonProperty("cache_control")
    private CacheControl cacheControl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentSource {
        private String type;

        @JsonProperty("media_type")
        private String mediaType;

        private String data;
    }

    private String data;

    /**
     * Only on a message_delta event: why generation stopped — "end_turn",
     * "max_tokens", "tool_use", "pause_turn" or "refusal". Null on content
     * block deltas (text_delta / thinking_delta / input_json_delta /
     * signature_delta), which is every other delta you receive.
     */
    @JsonProperty("stop_reason")
    private String stopReason;

    /**
     * Only on a message_delta event: the stop sequence that triggered the stop,
     * when stopReason is "stop_sequence".
     */
    @JsonProperty("stop_sequence")
    private String stopSequence;

    /**
     * Retained so existing call sites using the previous all-args constructor
     * keep compiling after stopReason / stopSequence were added.
     */
    @Deprecated
    public ChatMessageContent(String type, String text, String thinking, String signature, ContentSource source, String toolUseId, Object content, String id, String name, JsonNode input, String partialJson, CacheControl cacheControl, String data) {
        this.type = type;
        this.text = text;
        this.thinking = thinking;
        this.signature = signature;
        this.source = source;
        this.toolUseId = toolUseId;
        this.content = content;
        this.id = id;
        this.name = name;
        this.input = input;
        this.partialJson = partialJson;
        this.cacheControl = cacheControl;
        this.data = data;
    }
}