package me.vacuity.ai.sdk.gemini.decision.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.gemini.decision.entity.DecisionUsage;

import java.util.Map;

/**
 * Response from POST v1beta/decisions.
 * <p>
 * A safety block returns HTTP 400 with no {@code answers} at all rather than a
 * refusal marker on the individual answer, so a failed call surfaces as a
 * thrown exception, not a {@code Decision} with holes in it. The documented
 * codes are {@code safety}, {@code prohibited_content}, {@code blocklist},
 * {@code image_safety} and {@code content_blocked}.
 *
 * @author vacuity
 * @since 2.5.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Decision {

    /** Keyed by the same ids the request's {@code questions} map used. */
    private Map<String, DecisionAnswer> answers;

    private DecisionUsage usage;

    /**
     * The answer for one question id, or null if absent.
     */
    public DecisionAnswer answer(String questionId) {
        return answers == null ? null : answers.get(questionId);
    }
}
