package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token ceiling for a whole agentic loop. Unlike max_tokens (an enforced
 * per-response cap the model never sees), the task budget is surfaced to the
 * model so it can pace itself and finish gracefully.
 * <p>
 * Beta — send the "task-budgets-2026-03-13" flag via ChatRequest#betas.
 * Minimum total is 20000.
 *
 * @author: vacuity
 * @create: 2026-08-10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenTaskBudget {

    /**
     * Always "tokens".
     */
    private String type;

    /**
     * Total token budget for the task. Minimum 20000.
     */
    private Integer total;

    /**
     * Remaining budget. Defaults to total; only set it when you rewrite or
     * compact history yourself, otherwise the server tracks the countdown.
     */
    private Integer remaining;

    public static TokenTaskBudget tokens(Integer total) {
        return TokenTaskBudget.builder().type("tokens").total(total).build();
    }
}
