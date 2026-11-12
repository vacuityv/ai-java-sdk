package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * How many times server-side tools ran during the request.
 * <p>
 * Billing-relevant: web search and web fetch are charged per request, on top
 * of tokens, so a bill built from token counts alone misses them. Before 2.7.0
 * the SDK's {@link Usage} had no field for this.
 *
 * @author vacuity
 * @since 2.7.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerToolUsage {

    @JsonProperty("web_search_requests")
    private Integer webSearchRequests;

    @JsonProperty("web_fetch_requests")
    private Integer webFetchRequests;
}
