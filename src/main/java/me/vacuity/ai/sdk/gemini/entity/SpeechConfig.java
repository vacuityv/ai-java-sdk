package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Speech generation settings, for TTS models asked for an AUDIO modality.
 * <p>
 * The returned audio's container differs by model — measured
 * {@code audio/L16;codec=pcm;rate=24000} on gemini-2.5-flash-preview-tts and
 * {@code audio/wav} on gemini-3.8-flash-tts — so read the part's mime type
 * rather than assuming one.
 *
 * @author vacuity
 * @since 2.6.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpeechConfig {

    /** A single voice for the whole reading. */
    private VoiceConfig voiceConfig;

    /** Per-speaker voices instead; mutually exclusive with the above in practice. */
    private MultiSpeakerVoiceConfig multiSpeakerVoiceConfig;

    /** BCP-47, for example "en-US". */
    private String languageCode;

    /** Shorthand for one voice for the whole reading. */
    public static SpeechConfig voice(String voiceName) {
        return SpeechConfig.builder().voiceConfig(VoiceConfig.prebuilt(voiceName)).build();
    }
}
