package me.vacuity.ai.sdk.openai.realtime.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from {@code POST /v1/realtime/client_secrets}.
 *
 * @author vacuity
 * @since 2.7.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeClientSecret {

    /**
     * The ephemeral key — a secret. Hand it to the client that opens the
     * connection; do not log it.
     */
    private String value;

    /** Unix seconds after which {@link #value} stops working. */
    @JsonProperty("expires_at")
    private Long expiresAt;

    /** The session as the server resolved it, defaults filled in. */
    private RealtimeSessionConfig session;
}
