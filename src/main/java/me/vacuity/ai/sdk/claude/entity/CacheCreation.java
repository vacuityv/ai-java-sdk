package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cache-write tokens split by the lifetime they were written for.
 * <p>
 * The split is a billing requirement, not a detail: a 5-minute write costs
 * 1.25x the base input rate and a 1-hour write costs 2x, so
 * {@code cacheCreationInputTokens} alone cannot be priced. The two add up to
 * it.
 * <p>
 * Measured on claude-opus-5-5: a first call reported
 * {@code cache_creation_input_tokens} 4763 with
 * {@code ephemeral_5m_input_tokens} 4763 and {@code ephemeral_1h_input_tokens}
 * 0; the repeat reported zeros here and 4763 under
 * {@code cache_read_input_tokens}.
 *
 * @author vacuity
 * @since 2.3.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheCreation {

    /** Tokens written with the default 5-minute lifetime. Billed at 1.25x. */
    @JsonProperty("ephemeral_5m_input_tokens")
    private Integer ephemeral5mInputTokens;

    /** Tokens written with the 1-hour lifetime. Billed at 2x. */
    @JsonProperty("ephemeral_1h_input_tokens")
    private Integer ephemeral1hInputTokens;
}
