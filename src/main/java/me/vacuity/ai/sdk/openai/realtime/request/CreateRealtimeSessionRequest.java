package me.vacuity.ai.sdk.openai.realtime.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.entity.ChatTool;
import me.vacuity.ai.sdk.openai.realtime.entity.InputAudioTranscription;

import java.util.List;
import java.util.Set;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-01-14 14:15
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRealtimeSessionRequest {

    private Set<String> modalities;

    private String model;
    
    private String instructions;

    private String voice;

    @JsonProperty("input_audio_format")
    private String inputAudioFormat;

    @JsonProperty("output_audio_format")
    private String outputAudioFormat;

    @JsonProperty("input_audio_transcription")
    private InputAudioTranscription inputAudioTranscription;

    private List<ChatTool> tools;

    @JsonProperty("tool_choice")
    private String toolChoice;

    private Float temperature;

    @JsonProperty("max_response_output_tokens")
    private Object max_response_output_tokens;
}
