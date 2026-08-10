package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cache breakpoint marker for prompt caching.
 * Attach to a system text block, a message content block, or a tool definition;
 * everything before the marker (tools -> system -> messages) is cached.
 *
 * @author: vacuity
 * @create: 2026-08-10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CacheControl {

    /**
     * Always "ephemeral".
     */
    private String type;

    /**
     * Cache lifetime: "5m" (default) or "1h".
     */
    private String ttl;

    /**
     * Default 5-minute breakpoint.
     */
    public static CacheControl ephemeral() {
        return CacheControl.builder().type("ephemeral").build();
    }

    /**
     * Breakpoint with an explicit TTL ("5m" or "1h").
     */
    public static CacheControl ephemeral(String ttl) {
        return CacheControl.builder().type("ephemeral").ttl(ttl).build();
    }
}
