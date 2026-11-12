package me.vacuity.ai.sdk.gemini.decision.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Token usage for a decisions call.
 * <p>
 * Every field carries an explicit {@code @JsonProperty}: this endpoint is
 * snake_case, unlike the camelCase {@code usageMetadata} the rest of the
 * Gemini API returns, and the Gemini client's mapper sets no naming strategy —
 * so an unannotated field would simply never be populated.
 *
 * @author vacuity
 * @since 2.5.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionUsage {

    /** The only tokens billed by this endpoint. */
    @JsonProperty("total_input_tokens")
    private Integer totalInputTokens;

    @JsonProperty("input_tokens_by_modality")
    private List<ModalityTokenCount> inputTokensByModality;

    /**
     * Always 0 — the model emits probabilities, not text. Present so the field
     * can be logged alongside other endpoints' output counts rather than
     * special-cased.
     */
    @JsonProperty("total_output_tokens")
    private Integer totalOutputTokens;

    @JsonProperty("total_tokens")
    private Integer totalTokens;
}
