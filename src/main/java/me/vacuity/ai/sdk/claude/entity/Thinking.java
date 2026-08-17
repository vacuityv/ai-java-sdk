package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-02-25 09:13
 **/

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class Thinking {

    /**
     * "adaptive" (recommended), "enabled" (legacy), or "disabled".
     */
    private String type;

    /**
     * Visibility of the reasoning: "summarized" or "omitted".
     * <p>
     * Defaults to "omitted" on current models — thinking blocks are still
     * returned but their text is empty. Set "summarized" if you surface
     * reasoning to users. This controls visibility only; thinking happens and
     * is billed the same either way.
     */
    private String display;

    /**
     * Fixed thinking token budget.
     *
     * @deprecated Removed on current models (Opus 4.7 and later, Sonnet 5,
     * Fable 5) — sending it returns a 400. Use {@code type = "adaptive"} plus
     * {@link OutputConfig#getEffort()} instead. Still functional on Opus 4.6
     * and Sonnet 4.6 as a transitional escape hatch, where it must be less
     * than max_tokens.
     */
    @Deprecated
    @JsonProperty("budget_tokens")
    private Integer budgetTokens;

    /**
     * Retained so existing {@code new Thinking(type, budgetTokens)} call sites
     * keep compiling after the {@code display} field was added.
     */
    @Deprecated
    public Thinking(String type, Integer budgetTokens) {
        this.type = type;
        this.budgetTokens = budgetTokens;
    }

    /**
     * Adaptive thinking — the model decides when and how much to think.
     */
    public static Thinking adaptive() {
        return Thinking.builder().type("adaptive").build();
    }

    /**
     * Adaptive thinking with an explicit display mode ("summarized" or "omitted").
     */
    public static Thinking adaptive(String display) {
        return Thinking.builder().type("adaptive").display(display).build();
    }

    /**
     * Thinking off. Rejected on Fable 5, and on Opus 5 when effort is
     * "xhigh" or "max".
     */
    public static Thinking disabled() {
        return Thinking.builder().type("disabled").build();
    }
}
