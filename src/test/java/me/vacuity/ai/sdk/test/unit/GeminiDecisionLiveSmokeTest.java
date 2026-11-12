package me.vacuity.ai.sdk.test.unit;

import me.vacuity.ai.sdk.gemini.GeminiClient;
import me.vacuity.ai.sdk.gemini.decision.request.DecisionInput;
import me.vacuity.ai.sdk.gemini.decision.request.DecisionQuestion;
import me.vacuity.ai.sdk.gemini.decision.request.DecisionRequest;
import me.vacuity.ai.sdk.gemini.decision.response.Decision;
import me.vacuity.ai.sdk.gemini.decision.response.DecisionAnswer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Decisions API against the real Gemini endpoint. Skipped unless
 * GEMINI_API_KEY is set.
 * <p>
 * This is where the two documentation gaps are pinned: that an input block
 * needs a {@code type} the official field tables omit, and that a yes/no
 * question's {@code criteria} is honoured even though the page's own table
 * says it is unused. Both were established here, not read off the page.
 *
 * @author vacuity
 */
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
public class GeminiDecisionLiveSmokeTest {

    private static final String MODEL = "gemini-decision-1";

    private static final String INPUT = "The server returned HTTP 500 on 40% of requests "
            + "for the last 10 minutes. Error logs show connection pool exhaustion.";

    private GeminiClient client() {
        return new GeminiClient(System.getenv("GEMINI_API_KEY"), Duration.ofSeconds(120));
    }

    /**
     * All three question types in one call, which is also the cheapest way to
     * check them: questions share the input and are billed once for it.
     */
    @Test
    public void allThreeQuestionTypesAnswerInOneCall() {
        Map<String, String> causes = new LinkedHashMap<>();
        causes.put("pool", "database connection pool exhaustion");
        causes.put("deploy", "a bad deployment");
        causes.put("network", "a network partition");

        Map<String, DecisionQuestion> questions = new LinkedHashMap<>();
        questions.put("is_outage", DecisionQuestion.noul("Is this an active production outage?"));
        questions.put("root_cause", DecisionQuestion.choice(
                "What is the most likely root cause?", causes));
        questions.put("severity", DecisionQuestion.score("How severe is this?",
                Arrays.asList("negligible", "minor", "moderate", "major", "critical")));

        Decision decision = client().createDecision(DecisionRequest.builder()
                .model(MODEL).input(INPUT).questions(questions).build());

        DecisionAnswer outage = decision.answer("is_outage");
        assertNotNull(outage, "every question id must come back");
        assertNotNull(outage.getNoul(), "a noul answer must carry its probability");
        assertTrue(outage.getNoul() > 0.5,
                "connection pool exhaustion with 40% 500s reads as an outage; got "
                        + outage.getNoul());

        DecisionAnswer cause = decision.answer("root_cause");
        assertEquals("pool", cause.getChoice(),
                "the input names pool exhaustion outright; got " + cause.getChoice()
                        + " with " + cause.getProbabilities());
        assertEquals(3, cause.getProbabilities().size(),
                "the distribution must cover every option");

        DecisionAnswer severity = decision.answer("severity");
        assertNotNull(severity.getScore());
        assertNotNull(severity.getLegend(), "a score answer must carry its legend");
        assertEquals(5, severity.getLegend().size());

        // only input is billed, and the modality breakdown must arrive
        assertEquals(0, decision.getUsage().getTotalOutputTokens(),
                "this endpoint generates no text");
        assertTrue(decision.getUsage().getTotalInputTokens() > 0);
        assertEquals("text",
                decision.getUsage().getInputTokensByModality().get(0).getModality());
    }

    /**
     * An image block, which is the case that proves {@code type} really is
     * required and really is what the SDK sends — the official page's field
     * table for an image block does not mention it.
     */
    @Test
    public void anImageBlockIsAcceptedAndCountedAsItsOwnModality() throws Exception {
        Decision decision = client().createDecision(DecisionRequest.builder()
                .model(MODEL)
                .input(Arrays.asList(
                        DecisionInput.text("Screenshot from monitoring:"),
                        DecisionInput.image(redAlertPng(), "image/png")))
                .questions(Collections.singletonMap("what", DecisionQuestion.choice(
                        "What does the input show?", alertOptions())))
                .build());

        assertEquals("alert", decision.answer("what").getChoice(),
                "got " + decision.answer("what").getChoice()
                        + " with " + decision.answer("what").getProbabilities());

        boolean sawImage = false;
        for (me.vacuity.ai.sdk.gemini.decision.entity.ModalityTokenCount m
                : decision.getUsage().getInputTokensByModality()) {
            if ("image".equals(m.getModality())) {
                sawImage = true;
                assertTrue(m.getTokens() > 0);
            }
        }
        assertTrue(sawImage, "the image must be billed under its own modality: "
                + decision.getUsage().getInputTokensByModality());
    }

    /**
     * The page's field table says a yes/no question does not use criteria; its
     * examples pass one. The examples win — the criteria is billed and it
     * moves the answer, so the SDK offers the three-argument factory.
     */
    @Test
    public void noulCriteriaIsBilledAndActedOn() {
        Decision without = client().createDecision(DecisionRequest.builder()
                .model(MODEL).input(INPUT)
                .questions(Collections.singletonMap("q",
                        DecisionQuestion.noul("Is this an active production outage?")))
                .build());

        Decision with = client().createDecision(DecisionRequest.builder()
                .model(MODEL).input(INPUT)
                .questions(Collections.singletonMap("q", DecisionQuestion.noul(
                        "Is this an active production outage?",
                        "yes, an active outage", "no, not an outage")))
                .build());

        assertTrue(with.getUsage().getTotalInputTokens()
                        > without.getUsage().getTotalInputTokens(),
                "the criteria must reach the wire and be billed: "
                        + without.getUsage().getTotalInputTokens() + " -> "
                        + with.getUsage().getTotalInputTokens());
        assertNotNull(with.answer("q").getNoul());
    }

    /**
     * An unknown resolution is the server's complaint, which is why the SDK
     * keeps no enum of its own.
     */
    @Test
    public void anUnknownResolutionIsRejectedByTheServer() throws Exception {
        String png = redAlertPng();

        me.vacuity.ai.sdk.common.VacSdkException e =
                org.junit.jupiter.api.Assertions.assertThrows(
                        me.vacuity.ai.sdk.common.VacSdkException.class,
                        () -> client().createDecision(DecisionRequest.builder()
                                .model(MODEL)
                                .input(DecisionInput.image(png, "image/png", "bogus"))
                                .questions(Collections.singletonMap("what",
                                        DecisionQuestion.choice("What is it?",
                                                alertOptions())))
                                .build()));

        assertTrue(e.getMessage().contains("low"),
                "the server should list the accepted resolutions: " + e.getMessage());
    }

    private static Map<String, String> alertOptions() {
        Map<String, String> options = new LinkedHashMap<>();
        options.put("alert", "a warning or alert graphic");
        options.put("cat", "a cat");
        options.put("chart", "a data chart");
        return options;
    }

    /** A small red-bordered image, so the test needs no fixture on disk. */
    private static String redAlertPng() throws Exception {
        BufferedImage img = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(250, 250, 245));
        g.fillRect(0, 0, 320, 240);
        g.setColor(new Color(200, 30, 30));
        g.setStroke(new BasicStroke(8f));
        g.drawRect(40, 40, 240, 160);
        g.drawString("CRITICAL ALERT", 95, 125);
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return Base64.getEncoder().encodeToString(out.toByteArray());
    }
}
