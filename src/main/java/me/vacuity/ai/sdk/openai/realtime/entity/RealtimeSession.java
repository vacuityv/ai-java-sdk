package me.vacuity.ai.sdk.openai.realtime.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import me.vacuity.ai.sdk.openai.entity.ChatTool;

import java.util.List;
import java.util.Set;

/**
 * @description: openai realtime session object
 * https://platform.openai.com/docs/api-reference/realtime-sessions/session_object
 * @author: vacuity
 * @create: 2025-01-14 14:04
 **/

/**
 * @deprecated the beta Realtime session shape, for {@code POST /v1/realtime/sessions},
 * which has been removed upstream. Use {@link RealtimeSessionConfig} with
 * {@code OpenaiClient.createRealtimeClientSecret}.
 */
@Deprecated
@Data
public class RealtimeSession {

    @JsonProperty("client_secret")
    private ClientSecret clientSecret;

    private Set<String> modalities;

    private String instructions;

    private String voice;

    @JsonProperty("input_audio_format")
    private String inputAudioFormat;

    @JsonProperty("output_audio_format")
    private String outputAudioFormat;

    @JsonProperty("input_audio_transcription")
    private InputAudioTranscription inputAudioTranscription;

    @JsonProperty("turn_detection")
    private TurnDetection turnDetection;

    private List<ChatTool> tools;

    @JsonProperty("tool_choice")
    private String toolChoice;

    private Float temperature;

    @JsonProperty("max_response_output_tokens")
    private Object max_response_output_tokens;
    
}
