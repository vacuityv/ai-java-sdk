package me.vacuity.ai.sdk.openai.realtime.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.realtime.entity.RealtimeSessionConfig;

/**
 * Request body for {@code POST /v1/realtime/client_secrets}, which mints a
 * short-lived key a browser or device can use to open a Realtime connection
 * without holding the real API key.
 *
 * @author vacuity
 * @since 2.7.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateRealtimeClientSecretRequest {

    /** The session the key will open. Its {@code type} is required. */
    private RealtimeSessionConfig session;

    /** How long the key lives; the server default applies when absent. */
    @JsonProperty("expires_after")
    private ExpiresAfter expiresAfter;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ExpiresAfter {
        /** "created_at". */
        private String anchor;
        private Integer seconds;
    }
}
