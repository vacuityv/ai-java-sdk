package me.vacuity.ai.sdk.gemini.decision.constant;

/**
 * Question and answer type names used by the Decisions API, plus the input
 * block and resolution names.
 * <p>
 * Convenience only: nothing here is enforced. The server's own errors list the
 * accepted values — for an unknown resolution it answers "Supported values:
 * 'low', 'medium', 'high', ..." — and a client-side allowlist would outlive
 * the next value Google adds.
 *
 * @author vacuity
 * @since 2.5.0
 */
public class DecisionConstant {

    // ---- question / answer types

    /**
     * Yes/no. Google's name for it, not a typo: the answer carries a
     * {@code noul} probability and the probability of "no" is 1 minus it.
     */
    public static final String TYPE_NOUL = "noul";

    /** Pick one unordered option. */
    public static final String TYPE_CHOICE = "choice";

    /** Rate against ordered levels, lowest first. */
    public static final String TYPE_SCORE = "score";

    // ---- input block types

    public static final String INPUT_TEXT = "text";
    public static final String INPUT_IMAGE = "image";
    public static final String INPUT_AUDIO = "audio";
    public static final String INPUT_VIDEO = "video";
    public static final String INPUT_DOCUMENT = "document";

    // ---- resolutions

    public static final String RESOLUTION_LOW = "low";
    public static final String RESOLUTION_MEDIUM = "medium";
    public static final String RESOLUTION_HIGH = "high";

    /** Images only. */
    public static final String RESOLUTION_ULTRA_HIGH = "ultra_high";

    private DecisionConstant() {
    }
}
