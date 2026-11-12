package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt-caching controls on a chat-completions request.
 * <p>
 * The {@code prewarm} flag OpenAI's caching guide documents is deliberately
 * absent: this endpoint rejects it with
 * {@code Unknown parameter: 'prompt_cache_options.prewarm'}. The guide covers
 * the Responses API, where it does exist; modelling it here would only let
 * callers build a request that cannot succeed.
 *
 * @author vacuity
 * @since 2.6.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PromptCacheOptions {

    /**
     * "implicit" (cache automatically) or "explicit" (cache only at declared
     * breakpoints). The server names both when it rejects an unknown value.
     * <p>
     * Measured: with "implicit" a long prompt is written to cache as usual,
     * while with "explicit" neither a read nor a write was reported — nothing
     * is cached until something marks where.
     */
    private String mode;

    /**
     * Cache lifetime. Only {@code "30m"} is accepted on this endpoint —
     * {@code "12h"} answers
     * {@code Invalid value: '12h'. Supported values are: '30m'}, even though
     * the caching guide documents a 12-hour option (at 2.5x the write rate)
     * for the Responses API.
     */
    private String ttl;

    /** Shorthand for a mode with no explicit lifetime. */
    public static PromptCacheOptions mode(String mode) {
        return PromptCacheOptions.builder().mode(mode).build();
    }
}
