package me.vacuity.ai.sdk.openai.decision.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.decision.entity.DecisionUsage;

import java.util.List;

/**
 * Response from POST /v1/decisions.
 *
 * @author: vacuity
 * @create: 2026-10-08
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Decision {

    /**
     * One answer per question, each echoing the question's name.
     */
    private List<DecisionAnswer> answers;

    private String model;

    private DecisionUsage usage;

    /**
     * Looks up an answer by the question name it was asked under.
     *
     * @return the answer, or null if no question had that name
     */
    public DecisionAnswer answer(String name) {
        if (answers == null || name == null) {
            return null;
        }
        for (DecisionAnswer a : answers) {
            if (name.equals(a.getName())) {
                return a;
            }
        }
        return null;
    }
}
