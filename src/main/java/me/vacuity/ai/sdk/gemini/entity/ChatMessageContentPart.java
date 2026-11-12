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

    /** A file by URI — a Files API upload or a YouTube URL — instead of inline bytes. @since 2.7.0 */
    private me.vacuity.ai.sdk.gemini.entity.FileData fileData;
    /** Clip offsets and frame rate for a video part. @since 2.7.0 */
    private me.vacuity.ai.sdk.gemini.entity.VideoMetadata videoMetadata;
    /** Per-part media resolution, for example {@code {"level": "MEDIA_RESOLUTION_HIGH"}}. @since 2.7.0 */
    private java.util.Map<String, Object> mediaResolution;
    /** Transcription attached to an audio part ({@code text}, {@code words}, {@code speakerLabel}). @since 2.7.0 */
    private java.util.Map<String, Object> audioTranscription;
    /** Speaker and style for a speech part. @since 2.7.0 */
    private java.util.Map<String, Object> speechMetadata;
    /** Arbitrary metadata on the part. @since 2.7.0 */
    private java.util.Map<String, Object> partMetadata;
    /** "STATIC" or "AGENTIC". @since 2.7.0 */
    private String mediaProcessing;
}
