package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:27
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageContentPart {

    private Boolean thought = Boolean.FALSE;

    private String text;

    private InlineData inlineData;

    private ChatFunctionCall functionCall;

    private FunctionResponse functionResponse;

    private ExecutableCode executableCode;

    private CodeExecutionResult codeExecutionResult;

    @JsonProperty("thoughtSignature")
    private String thoughtSignature;

    /**
     * A tool the model ran server-side. Only present when the request set
     * {@code toolConfig.includeServerSideToolInvocations}.
     */
    private ServerToolCall toolCall;

    /**
     * The result of that server-side tool run. Only present when the request
     * set {@code toolConfig.includeServerSideToolInvocations}.
     */
    private ServerToolResponse toolResponse;

    /**
     * 兜底捕获服务端返回但 SDK 未声明的字段。
     * <p>
     * Gemini 3 / 4 要求多轮对话把上一轮 model 的 part 原样回传（thoughtSignature
     * 尤其如此）。没有这个兜底，未建模的字段会在反序列化时被静默丢弃，回传出去的
     * part 就残缺了。
     */
    @lombok.Builder.Default
    private java.util.Map<String, Object> extra = new java.util.HashMap<>();

    @com.fasterxml.jackson.annotation.JsonAnySetter
    public void putExtra(String key, Object value) {
        if (extra == null) extra = new java.util.HashMap<>();
        extra.put(key, value);
    }

    @com.fasterxml.jackson.annotation.JsonAnyGetter
    public java.util.Map<String, Object> getExtra() {
        return extra;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InlineData {

        private String mimeType;

        private String data;
    }
}
