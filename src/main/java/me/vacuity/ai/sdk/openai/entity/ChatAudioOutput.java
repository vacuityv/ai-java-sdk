package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Audio returned on a chat-completions message, when the request asked for an
 * audio modality.
 *
 * @author vacuity
 * @since 2.7.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAudioOutput {

    /** Pass back as {@code {"audio": {"id": ...}}} in a later turn to refer to this audio. */
    private String id;

    /** Unix seconds after which {@link #id} can no longer be referenced. */
    @JsonProperty("expires_at")
    private Long expiresAt;

    /** Base64 audio, in the format the request asked for. */
    private String data;

    private String transcript;
}
