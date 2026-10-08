package me.vacuity.ai.sdk.test.unit;

import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.decision.constant.DecisionConstant;
import me.vacuity.ai.sdk.openai.decision.request.DecisionQuestion;
import me.vacuity.ai.sdk.openai.decision.request.DecisionRequest;
import me.vacuity.ai.sdk.openai.decision.response.Decision;
import me.vacuity.ai.sdk.openai.decision.response.DecisionAnswer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke tests against the real Decisions API. Skipped unless VAC_OPENAI_KEY is
 * set. Only input tokens are billed here and the inputs are tiny, so a full run
 * costs a fraction of a cent.
 */
@EnabledIfEnvironmentVariable(named = "VAC_OPENAI_KEY", matches = ".+")
public class OpenaiDecisionLiveSmokeTest {

    private static final String MODEL = "gpt-6-luna";

    private OpenaiClient client() {
        String baseUrl = System.getenv("VAC_OPENAI_BASE_URL");
        String key = System.getenv("VAC_OPENAI_KEY");
        return baseUrl == null || baseUrl.isEmpty()
                ? new OpenaiClient(key, Duration.ofSeconds(60))
                : new OpenaiClient(key, Duration.ofSeconds(60), baseUrl);
    }

    /**
     * All three question types in one request, which is also the documented way
     * to batch independent questions.
     */
    @Test
    public void allThreeQuestionTypesInOneRequest() {
        Decision decision = client().createDecision(DecisionRequest.builder()
                .model(MODEL)
                .input("The app crashes every time I open the settings page on iOS 18.")
                .questions(Arrays.asList(
                        DecisionQuestion.predicate("is_bug",
                                "Does the text describe a software defect?"),
                        DecisionQuestion.choice("area", "Which area does this belong to?",
                                Arrays.asList(
                                        DecisionQuestion.Choice.of("mobile", "iOS or Android client"),
                                        DecisionQuestion.Choice.of("backend", "server side"),
                                        DecisionQuestion.Choice.of("docs", "documentation"))),
                        DecisionQuestion.score("severity", "How severe is this issue?",
                                Arrays.asList(
                                        DecisionQuestion.Level.of("low", "cosmetic"),
                                        DecisionQuestion.Level.of("medium", "degraded but usable"),
                                        DecisionQuestion.Level.of("high", "unusable or data loss")))))
                .build());

        assertNotNull(decision);
        assertEquals(MODEL, decision.getModel());
        assertEquals(3, decision.getAnswers().size(), "one answer per question");

        DecisionAnswer bug = decision.answer("is_bug");
        assertEquals(DecisionConstant.PREDICATE, bug.getType());
        assertNotNull(bug.getProbability());
        assertTrue(bug.getProbability() > 0.5,
                "a crash report should read as a defect, got " + bug.getProbability());

        DecisionAnswer area = decision.answer("area");
        assertEquals(DecisionConstant.CHOICE, area.getType());
        assertEquals("mobile", area.getChoice(), "an iOS crash should route to mobile");
        assertEquals(3, area.getProbabilities().size(),
                "the distribution should cover every supplied choice");
        assertNotNull(area.getConfidence());

        DecisionAnswer severity = decision.answer("severity");
        assertEquals(DecisionConstant.SCORE, severity.getType());
        assertNotNull(severity.getScore());
        // score is a weighted average of level indexes, so it stays within range
        assertTrue(severity.getScore() >= 0 && severity.getScore() <= 2,
                "score must fall inside the level index range, got " + severity.getScore());
        assertEquals("high", severity.getProbabilities().get(2).getLabel(),
                "levels come back in the order they were supplied");
    }

    /**
     * Boolean choice values are allowed alongside strings, and only input
     * tokens are billed.
     */
    @Test
    public void booleanChoicesAndInputOnlyBilling() {
        Decision decision = client().createDecision(DecisionRequest.builder()
                .model(MODEL)
                .input("Thanks, that fixed it! Closing the ticket.")
                .questions(Collections.singletonList(
                        DecisionQuestion.choice("resolved", "Is the issue resolved?",
                                Arrays.asList(
                                        DecisionQuestion.Choice.of(true, "resolved"),
                                        DecisionQuestion.Choice.of(false, "still open")))))
                .build());

        DecisionAnswer resolved = decision.answer("resolved");
        assertEquals(Boolean.TRUE, resolved.getChoice(),
                "boolean choice values should round-trip as booleans");

        assertNotNull(decision.getUsage());
        assertTrue(decision.getUsage().getInputTokens() > 0);
        assertEquals(0, decision.getUsage().getOutputTokens(),
                "the Decisions API bills input only and reports zero output tokens");
    }
}
