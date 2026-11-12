package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.vacuity.ai.sdk.gemini.GeminiClient;
import me.vacuity.ai.sdk.gemini.decision.request.DecisionInput;
import me.vacuity.ai.sdk.gemini.decision.request.DecisionQuestion;
import me.vacuity.ai.sdk.gemini.decision.request.DecisionRequest;
import me.vacuity.ai.sdk.gemini.decision.response.Decision;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Gemini Decisions API, request and response.
 * <p>
 * Two things here are easy to get silently wrong, and both have a test:
 * <ul>
 * <li>This endpoint is snake_case, unlike the camelCase the rest of the Gemini
 * API uses, and the Gemini client's mapper sets no naming strategy. A field
 * missing its {@code @JsonProperty} would read as null forever.</li>
 * <li>Input blocks need a {@code type}, which the official page's field tables
 * omit. A block without it is rejected outright.</li>
 * </ul>
 * The response bodies below are verbatim from live calls made on 2026-11-12.
 *
 * @author vacuity
 */
public class GeminiDecisionTest {

    private final ObjectMapper mapper = GeminiClient.defaultObjectMapper();

    /** Verbatim live response: one yes/no question. */
    private static final String NOUL = "{\"answers\":{\"is_outage\":{"
            + "\"confidence\":0.9514462775721875,\"noul\":0.9514462775721875,"
            + "\"type\":\"noul\"}},\"usage\":{\"total_tokens\":119,"
            + "\"total_input_tokens\":119,\"input_tokens_by_modality\":["
            + "{\"modality\":\"text\",\"tokens\":119}],\"total_output_tokens\":0}}";

    /** Verbatim live response: a choice and a score in one call. */
    private static final String CHOICE_AND_SCORE = "{\"answers\":{"
            + "\"severity\":{\"confidence\":0.5049296246508663,\"score\":3.1136380211842933,"
            + "\"probabilities\":{\"2\":0.15914732779340438,\"0\":0.001987486704655408,"
            + "\"3\":0.5049296246508663,\"4\":0.31553964359793724,\"1\":0.01839591725313663},"
            + "\"legend\":{\"0\":\"negligible\",\"4\":\"critical\",\"2\":\"moderate\","
            + "\"3\":\"major\",\"1\":\"minor\"},\"type\":\"score\"},"
            + "\"root_cause\":{\"confidence\":0.9976273290565881,\"choice\":\"pool\","
            + "\"probabilities\":{\"network\":0.0008333393008270936,"
            + "\"deploy\":0.0015393316425848461,\"pool\":0.9976273290565881},"
            + "\"type\":\"choice\"}},\"usage\":{\"total_tokens\":193,"
            + "\"total_input_tokens\":193,\"input_tokens_by_modality\":["
            + "{\"modality\":\"text\",\"tokens\":193}],\"total_output_tokens\":0}}";

    // ------------------------------------------------------------- 响应解析

    /**
     * The snake_case usage block. If the Gemini mapper's lack of a naming
     * strategy were ever papered over with one, or a {@code @JsonProperty}
     * went missing, this is what would catch it.
     */
    @Test
    public void snakeCaseUsageFieldsDeserialize() throws Exception {
        Decision decision = mapper.readValue(NOUL, Decision.class);

        assertNotNull(decision.getUsage(), "usage must deserialize");
        assertEquals(119, decision.getUsage().getTotalInputTokens());
        assertEquals(119, decision.getUsage().getTotalTokens());
        assertEquals(0, decision.getUsage().getTotalOutputTokens(),
                "this endpoint generates no text, so output is always 0");
        assertEquals(1, decision.getUsage().getInputTokensByModality().size());
        assertEquals("text",
                decision.getUsage().getInputTokensByModality().get(0).getModality());
        assertEquals(119, decision.getUsage().getInputTokensByModality().get(0).getTokens());
    }

    @Test
    public void noulAnswerDeserializes() throws Exception {
        Decision decision = mapper.readValue(NOUL, Decision.class);

        assertEquals(0.9514462775721875, decision.answer("is_outage").getNoul());
        assertEquals("noul", decision.answer("is_outage").getType());
        // for yes/no, confidence is max(noul, 1 - noul)
        assertEquals(decision.answer("is_outage").getNoul(),
                decision.answer("is_outage").getConfidence());
        // a noul answer carries no distribution of its own
        assertNull(decision.answer("is_outage").getProbabilities());
        assertNull(decision.answer("is_outage").getLegend());
    }

    @Test
    public void choiceAnswerDeserializes() throws Exception {
        Decision decision = mapper.readValue(CHOICE_AND_SCORE, Decision.class);

        assertEquals("pool", decision.answer("root_cause").getChoice());
        assertEquals(3, decision.answer("root_cause").getProbabilities().size());
        // the distribution covers every option and sums to 1
        double sum = 0;
        for (Double p : decision.answer("root_cause").getProbabilities().values()) {
            sum += p;
        }
        assertTrue(Math.abs(sum - 1.0) < 1e-9, "probabilities must sum to 1, got " + sum);
        // confidence is the winning option's probability
        assertEquals(decision.answer("root_cause").getProbabilities().get("pool"),
                decision.answer("root_cause").getConfidence());
        assertNull(decision.answer("root_cause").getLegend(), "legend is score-only");
    }

    /**
     * A score is a weighted average of indexes, so it is fractional and is not
     * itself an index — reading it as one would land on the wrong level.
     */
    @Test
    public void scoreAnswerIsFractionalAndCarriesALegend() throws Exception {
        Decision decision = mapper.readValue(CHOICE_AND_SCORE, Decision.class);

        assertEquals(3.1136380211842933, decision.answer("severity").getScore());
        assertEquals(5, decision.answer("severity").getProbabilities().size());
        assertEquals("critical", decision.answer("severity").getLegend().get("4"));
        assertEquals("negligible", decision.answer("severity").getLegend().get("0"));
        // the legend is keyed by index as a string, and covers every level
        assertEquals(decision.answer("severity").getProbabilities().keySet(),
                decision.answer("severity").getLegend().keySet());
        assertNull(decision.answer("severity").getChoice(), "choice is choice-only");
    }

    @Test
    public void answerLookupToleratesAnUnknownId() throws Exception {
        Decision decision = mapper.readValue(NOUL, Decision.class);

        assertNull(decision.answer("no_such_question"));
        assertNull(new Decision().answer("anything"), "no answers map at all");
    }

    @Test
    public void unknownResponseFieldsDoNotFailDeserialization() throws Exception {
        Decision decision = mapper.readValue(
                "{\"answers\":{\"q\":{\"type\":\"noul\",\"noul\":0.5,\"future\":1}},"
                        + "\"usage\":{\"total_tokens\":1,\"future_total\":2},\"extra\":3}",
                Decision.class);

        assertEquals(0.5, decision.answer("q").getNoul());
        assertEquals(1, decision.getUsage().getTotalTokens());
    }

    // ------------------------------------------------------------- 请求序列化

    /**
     * Input blocks are flat and carry a required {@code type}. The official
     * page's field tables leave it out; the server answers
     * {@code The 'type' parameter is required at 'input[0]'} without it.
     */
    @Test
    public void inputBlocksAreFlatAndCarryTheirType() throws Exception {
        DecisionRequest request = DecisionRequest.builder()
                .model("gemini-decision-1")
                .input(Arrays.asList(
                        DecisionInput.text("Screenshot from monitoring:"),
                        DecisionInput.image("YmFzZTY0", "image/png", "low")))
                .questions(Collections.singletonMap("what",
                        DecisionQuestion.noul("Is this an alert?")))
                .build();

        JsonNode sent = mapper.valueToTree(request);
        JsonNode blocks = sent.get("input");
        assertTrue(blocks.isArray());

        assertEquals("text", blocks.get(0).get("type").asText());
        assertEquals("Screenshot from monitoring:", blocks.get(0).get("text").asText());
        assertEquals(2, blocks.get(0).size(),
                "a text block sends only type and text: " + blocks.get(0));

        assertEquals("image", blocks.get(1).get("type").asText());
        assertEquals("YmFzZTY0", blocks.get(1).get("data").asText());
        assertEquals("low", blocks.get(1).get("resolution").asText());
        // snake_case, and the Gemini mapper applies no naming strategy of its own
        assertEquals("image/png", blocks.get(1).get("mime_type").asText());
        assertNull(blocks.get(1).get("mimeType"), "the Java name must not reach the wire");
        assertNull(blocks.get(1).get("uri"), "an unset source must not be sent");
    }

    @Test
    public void plainStringInputStaysAString() throws Exception {
        DecisionRequest request = DecisionRequest.builder()
                .model("gemini-decision-1")
                .input("a plain text description")
                .questions(Collections.singletonMap("q",
                        DecisionQuestion.noul("Is it text?")))
                .build();

        JsonNode sent = mapper.valueToTree(request);
        assertTrue(sent.get("input").isTextual(),
                "a String input must not be wrapped into a block");
        assertEquals("a plain text description", sent.get("input").asText());
    }

    @Test
    public void aSingleInputBlockIsNotWrappedInAList() throws Exception {
        DecisionRequest request = DecisionRequest.builder()
                .model("gemini-decision-1")
                .input(DecisionInput.document("JVBERi0="))
                .questions(Collections.singletonMap("q", DecisionQuestion.noul("?")))
                .build();

        JsonNode sent = mapper.valueToTree(request);
        assertTrue(sent.get("input").isObject(), "a single block stays an object");
        assertEquals("document", sent.get("input").get("type").asText());
        assertEquals("application/pdf", sent.get("input").get("mime_type").asText());
    }

    /**
     * The three question types each put their criteria in a different shape,
     * and a noul question may carry none at all.
     */
    @Test
    public void questionTypesSerializeTheirOwnCriteriaShape() throws Exception {
        Map<String, DecisionQuestion> questions = new LinkedHashMap<>();
        questions.put("plain", DecisionQuestion.noul("Is this an outage?"));
        questions.put("described", DecisionQuestion.noul(
                "Is this an outage?", "yes, an active outage", "no, not an outage"));
        Map<String, String> options = new LinkedHashMap<>();
        options.put("pool", "connection pool exhaustion");
        options.put("deploy", "a bad deployment");
        questions.put("cause", DecisionQuestion.choice("Root cause?", options));
        questions.put("severity", DecisionQuestion.score(
                "How severe?", Arrays.asList("minor", "moderate", "critical")));

        JsonNode sent = mapper.valueToTree(DecisionRequest.builder()
                .model("gemini-decision-1").input("x").questions(questions).build());
        JsonNode q = sent.get("questions");

        // noul without criteria sends none — not an empty object, not null
        assertEquals("noul", q.get("plain").get("type").asText());
        assertNull(q.get("plain").get("criteria"));
        assertEquals(2, q.get("plain").size());

        // noul with criteria uses the two description keys
        assertEquals("yes, an active outage",
                q.get("described").get("criteria").get("true_description").asText());
        assertEquals("no, not an outage",
                q.get("described").get("criteria").get("false_description").asText());

        // choice criteria is a map of option key to description
        assertTrue(q.get("cause").get("criteria").isObject());
        assertEquals("a bad deployment", q.get("cause").get("criteria").get("deploy").asText());

        // score criteria is an ordered list, lowest first — the order is load-bearing
        assertTrue(q.get("severity").get("criteria").isArray());
        assertEquals("minor", q.get("severity").get("criteria").get(0).asText());
        assertEquals("critical", q.get("severity").get("criteria").get(2).asText());
    }

    @Test
    public void questionsAreKeyedNotListed() throws Exception {
        JsonNode sent = mapper.valueToTree(DecisionRequest.builder()
                .model("gemini-decision-1").input("x")
                .questions(Collections.singletonMap("my_id", DecisionQuestion.noul("?")))
                .build());

        assertTrue(sent.get("questions").isObject(),
                "Gemini keys its questions by id, where OpenAI's Decisions API lists them");
        assertNotNull(sent.get("questions").get("my_id"));
    }
}
