package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Breakdown of the completion tokens, as returned by OpenAI-compatible
 * chat-completions endpoints.
 * <p>
 * All four fields come back on a plain text call against OpenAI; DeepSeek
 * documents only {@code reasoning_tokens} and does not mark the enclosing
 * object as always present.
 *
 * @author vacuity
 * @since 2.2.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletionTokensDetails {

    /**
     * Tokens the model generated while reasoning. Already counted in
     * {@code completionTokens}, so it is a subset, not an extra charge.
     */
    @JsonProperty("reasoning_tokens")
    private Integer reasoningTokens;

    /** Audio tokens in the completion. Reported as 0 on text-only calls. */
    @JsonProperty("audio_tokens")
    private Integer audioTokens;

    /** Predicted-output tokens that matched and were accepted. */
    @JsonProperty("accepted_prediction_tokens")
    private Integer acceptedPredictionTokens;

    /** Predicted-output tokens that did not match and were billed anyway. */
    @JsonProperty("rejected_prediction_tokens")
    private Integer rejectedPredictionTokens;
}
