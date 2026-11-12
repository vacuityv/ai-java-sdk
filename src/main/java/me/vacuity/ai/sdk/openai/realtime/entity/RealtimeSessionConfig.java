package me.vacuity.ai.sdk.openai.realtime.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * A GA Realtime session configuration — the {@code session} object sent to
 * {@code POST /v1/realtime/client_secrets} and returned inside its response.
 * <p>
 * This is not the shape the deprecated {@link RealtimeSession} used. Audio
 * settings now nest under {@link #audio} ({@code audio.input.format},
 * {@code audio.output.voice}) instead of sitting at the top level as
 * {@code input_audio_format} / {@code voice}, and {@code modalities} became
 * {@link #outputModalities}. Field names follow OpenAI's
 * {@code RealtimeSessionCreateRequestGA} schema.
 * <p>
 * Several fields are unions in the spec and are typed loosely here so any
 * documented variant passes through unchanged: {@link #maxOutputTokens} is an
 * integer or {@code "inf"}; {@link #toolChoice} a string or an object;
 * {@link #tracing} and {@link #truncation} a string or an object.
 *
 * @author vacuity
 * @since 2.7.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RealtimeSessionConfig {

    /** "realtime" for a speech session. Required on the request. */
    private String type;

    private String model;

    private String instructions;

    /** For example ["audio"] or ["text"]. */
    @JsonProperty("output_modalities")
    private List<String> outputModalities;

    private Audio audio;

    /** Extra fields to include in server events, for example "item.input_audio_transcription.logprobs". */
    private List<String> include;

    /** An integer, or the string "inf". */
    @JsonProperty("max_output_tokens")
    private Object maxOutputTokens;

    /** "auto", "none", "required", or an object naming a function. */
    @JsonProperty("tool_choice")
    private Object toolChoice;

    private List<Object> tools;

    /** "auto", or an object configuring tracing. */
    private Object tracing;

    /** "auto", "disabled", or an object. */
    private Object truncation;

    /** A reusable prompt reference. */
    private Map<String, Object> prompt;

    private Reasoning reasoning;

    @JsonProperty("parallel_tool_calls")
    private Boolean parallelToolCalls;

    // ---- present on the response only

    /** Session id, on the response. */
    private String id;

    /** "realtime.session", on the response. */
    private String object;

    /** Unix seconds, on the response. */
    @JsonProperty("expires_at")
    private Long expiresAt;

    /** Audio input and output settings. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Audio {
        private AudioInput input;
        private AudioOutput output;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AudioInput {
        private AudioFormat format;

        /** For example {"type": "near_field"} or {"type": "far_field"}; null to disable. */
        @JsonProperty("noise_reduction")
        private Map<String, Object> noiseReduction;

        private Transcription transcription;

        /** Server or semantic VAD; null turns automatic turn detection off. */
        @JsonProperty("turn_detection")
        private TurnDetection turnDetection;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AudioOutput {
        private AudioFormat format;

        /** A voice name such as "alloy", or an object {"id": ...} for a custom voice. */
        private Object voice;

        private Double speed;
    }

    /** For example {type: "audio/pcm", rate: 24000}, or {type: "audio/pcmu"}. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AudioFormat {
        private String type;
        private Integer rate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Transcription {
        private String model;
        private String language;
        private List<String> languages;
        private String prompt;
        private List<String> keywords;
        /** "minimal", "low", "medium", "high" or "xhigh". */
        private String delay;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TurnDetection {
        /** "server_vad" or "semantic_vad". */
        private String type;
        private Double threshold;
        @JsonProperty("prefix_padding_ms")
        private Integer prefixPaddingMs;
        @JsonProperty("silence_duration_ms")
        private Integer silenceDurationMs;
        @JsonProperty("create_response")
        private Boolean createResponse;
        @JsonProperty("interrupt_response")
        private Boolean interruptResponse;
        /** semantic_vad only: "low", "medium", "high" or "auto". */
        private String eagerness;
        @JsonProperty("idle_timeout_ms")
        private Integer idleTimeoutMs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Reasoning {
        /** "minimal", "low", "medium", "high" or "xhigh". */
        private String effort;
    }
}
