package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One speaker in a multi-speaker setup. {@code speaker} must match the name
 * used in the prompt text.
 *
 * @author vacuity
 * @since 2.6.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpeakerVoiceConfig {

    private String speaker;

    private VoiceConfig voiceConfig;

    public static SpeakerVoiceConfig of(String speaker, String voiceName) {
        return SpeakerVoiceConfig.builder()
                .speaker(speaker).voiceConfig(VoiceConfig.prebuilt(voiceName)).build();
    }
}
