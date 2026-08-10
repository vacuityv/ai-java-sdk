package me.vacuity.ai.sdk.openai.responses.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Options for prompt caching. Supports implicit or explicit cache breakpoints.
 *
 * @author: vacuity
 * @create: 2026-08-10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PromptCacheOptions {

    /**
     * Controls whether OpenAI automatically creates an implicit cache breakpoint.
     * Possible values: "implicit", "explicit".
     */
    private String mode;

    /**
     * Minimum lifetime for cache breakpoints.
     * Defaults to "30m", currently the only supported value.
     */
    private String ttl;
}
