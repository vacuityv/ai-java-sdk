package me.vacuity.ai.sdk.gemini.decision.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One question put to the Decisions API. Each is scored independently against
 * the shared input.
 *
 * @author vacuity
 * @since 2.5.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DecisionQuestion {

    /** "noul" (yes/no), "choice" or "score". */
    private String type;

    /** What to decide. Text only. */
    private String instructions;

    /**
     * Shape depends on {@code type}:
     * <ul>
     * <li>"choice": a map of option key to description, 2-255 entries. A
     * description may be empty.</li>
     * <li>"score": an ordered list of non-empty labels, lowest first, 2-255
     * entries (2-10 recommended). The order is load-bearing — the answer's
     * {@code score} is a weighted average of the indexes.</li>
     * <li>"noul": optional, a map with "true_description" and
     * "false_description".</li>
     * </ul>
     */
    private Object criteria;

    /**
     * A yes/no question. The answer carries {@code noul}, the probability of
     * yes; the probability of no is 1 minus it.
     */
    public static DecisionQuestion noul(String instructions) {
        return DecisionQuestion.builder().type("noul").instructions(instructions).build();
    }

    /**
     * A yes/no question with both sides described.
     * <p>
     * The official page's own field table says criteria is unused for "noul",
     * while its examples pass one. Measured on 2026-11-12, the examples are
     * right: the same question answered 0.9514 without criteria and 0.9464
     * with, and the input token count rose from 119 to 149 — so it is sent,
     * billed, and acted on. Optional, but not ignored.
     */
    public static DecisionQuestion noul(String instructions,
                                        String trueDescription,
                                        String falseDescription) {
        Map<String, String> criteria = new LinkedHashMap<>();
        criteria.put("true_description", trueDescription);
        criteria.put("false_description", falseDescription);
        return DecisionQuestion.builder()
                .type("noul").instructions(instructions).criteria(criteria).build();
    }

    /**
     * Pick one of several unordered options, given as key to description. The
     * answer carries the winning key in {@code choice} plus the full
     * {@code probabilities} map.
     * <p>
     * Pass a {@link LinkedHashMap} if the order matters to you for reading —
     * the server sorts option keys by UTF-8 byte order before evaluating
     * either way, and breaks ties toward the alphabetically first key.
     */
    public static DecisionQuestion choice(String instructions, Map<String, String> options) {
        return DecisionQuestion.builder()
                .type("choice").instructions(instructions).criteria(options).build();
    }

    /**
     * Rate against ordered levels, lowest first. The answer carries a
     * fractional {@code score} over the level indexes, a {@code legend}
     * mapping index to label, and the {@code probabilities} map.
     */
    public static DecisionQuestion score(String instructions, List<String> levels) {
        return DecisionQuestion.builder()
                .type("score").instructions(instructions).criteria(levels).build();
    }
}
