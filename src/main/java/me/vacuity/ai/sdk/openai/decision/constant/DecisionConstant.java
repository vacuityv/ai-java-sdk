package me.vacuity.ai.sdk.openai.decision.constant;

/**
 * Question and answer type discriminators for the Decisions API.
 *
 * @author: vacuity
 * @create: 2026-10-08
 **/
public class DecisionConstant {

    /** Estimate the probability that a statement is true. */
    public static final String PREDICATE = "predicate";

    /** Pick one of a set of unordered categories. */
    public static final String CHOICE = "choice";

    /** Rate against ordered levels. */
    public static final String SCORE = "score";

    /** Answer type only: the model declined to answer this question. */
    public static final String REFUSAL = "refusal";
}
