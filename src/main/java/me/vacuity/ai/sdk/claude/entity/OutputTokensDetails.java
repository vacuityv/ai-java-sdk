package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Read-only breakdown of the billed output tokens.
 *
 * @author: vacuity
 * @create: 2026-09-10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutputTokensDetails {

    /**
     * How many of the billed output tokens were spent on internal reasoning.
     * Reflects the raw reasoning generated, not the summarized text returned,
     * so it is always less than or equal to outputTokens. When streaming, it
     * only appears on the final message_delta event.
     */
    @JsonProperty("thinking_tokens")
    private Integer thinkingTokens;
}
