package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Which voice to speak with.
 *
 * @author vacuity
 * @since 2.6.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoiceConfig {

    private PrebuiltVoiceConfig prebuiltVoiceConfig;

    /** An alternative to {@link #prebuiltVoiceConfig}, per the discovery document. */
    private String voice;

    /** Shorthand for one of the API's named voices. */
    public static VoiceConfig prebuilt(String voiceName) {
        return VoiceConfig.builder()
                .prebuiltVoiceConfig(PrebuiltVoiceConfig.builder().voiceName(voiceName).build())
                .build();
    }
}
