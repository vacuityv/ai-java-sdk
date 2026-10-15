package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.vacuity.ai.sdk.claude.ClaudeClient;
import me.vacuity.ai.sdk.claude.response.ChatResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Parsing tests for the Claude response fields added alongside refusals and
 * prompt caching.
 */
public class ClaudeResponseParsingTest {

    private static final ObjectMapper MAPPER = ClaudeClient.defaultObjectMapper();

    @Test
    public void parsesRefusalStopDetails() throws Exception {
        String body = "{"
                + "\"id\":\"msg_1\",\"type\":\"message\",\"role\":\"assistant\","
                + "\"model\":\"claude-opus-5\",\"content\":[],"
                + "\"stop_reason\":\"refusal\","
                + "\"stop_details\":{\"type\":\"refusal\",\"category\":\"cyber\",\"explanation\":\"declined\"}"
                + "}";

        ChatResponse response = MAPPER.readValue(body, ChatResponse.class);

        assertEquals("refusal", response.getStopReason());
        assertNotNull(response.getStopDetails());
        assertEquals("refusal", response.getStopDetails().getType());
        assertEquals("cyber", response.getStopDetails().getCategory());
        assertEquals("declined", response.getStopDetails().getExplanation());
    }

    @Test
    public void stopDetailsIsNullOnNormalCompletion() throws Exception {
        String body = "{\"id\":\"msg_1\",\"type\":\"message\",\"role\":\"assistant\","
                + "\"model\":\"claude-opus-5\",\"content\":[],\"stop_reason\":\"end_turn\"}";

        ChatResponse response = MAPPER.readValue(body, ChatResponse.class);

        assertEquals("end_turn", response.getStopReason());
        assertNull(response.getStopDetails());
    }

    @Test
    public void parsesCacheTokenCountsFromUsage() throws Exception {
        String body = "{\"id\":\"msg_1\",\"type\":\"message\",\"role\":\"assistant\","
                + "\"model\":\"claude-opus-5\",\"content\":[],\"stop_reason\":\"end_turn\","
                + "\"usage\":{\"input_tokens\":10,\"output_tokens\":20,"
                + "\"cache_creation_input_tokens\":1024,\"cache_read_input_tokens\":2048,"
                + "\"service_tier\":\"standard\"}}";

        ChatResponse response = MAPPER.readValue(body, ChatResponse.class);

        assertEquals(10, response.getUsage().getInputTokens());
        assertEquals(20, response.getUsage().getOutputTokens());
        assertEquals(1024, response.getUsage().getCacheCreationInputTokens());
        assertEquals(2048, response.getUsage().getCacheReadInputTokens());
        assertEquals("standard", response.getUsage().getServiceTier());
    }

    @Test
    public void cacheCountsAreNullWhenAbsent() throws Exception {
        String body = "{\"id\":\"msg_1\",\"type\":\"message\",\"role\":\"assistant\","
                + "\"model\":\"claude-opus-5\",\"content\":[],"
                + "\"usage\":{\"input_tokens\":10,\"output_tokens\":20}}";

        ChatResponse response = MAPPER.readValue(body, ChatResponse.class);

        assertNull(response.getUsage().getCacheReadInputTokens());
        assertNull(response.getUsage().getCacheCreationInputTokens());
    }

    @Test
    public void unknownServerFieldsDoNotBreakParsing() throws Exception {
        String body = "{\"id\":\"msg_1\",\"type\":\"message\",\"role\":\"assistant\","
                + "\"model\":\"claude-opus-5\",\"content\":[],"
                + "\"some_field_added_next_quarter\":{\"nested\":true}}";

        ChatResponse response = MAPPER.readValue(body, ChatResponse.class);

        assertEquals("msg_1", response.getId());
    }

    @Test
    public void parsesThinkingTokenBreakdown() throws Exception {
        String body = "{\"id\":\"msg_1\",\"stop_reason\":\"end_turn\","
                + "\"usage\":{\"input_tokens\":25,\"output_tokens\":348,"
                + "\"output_tokens_details\":{\"thinking_tokens\":312}}}";

        ChatResponse response = MAPPER.readValue(body, ChatResponse.class);

        assertNotNull(response.getUsage().getOutputTokensDetails());
        assertEquals(312, response.getUsage().getOutputTokensDetails().getThinkingTokens());
        // 思考 token 是 output_tokens 的子集
        assertEquals(348, response.getUsage().getOutputTokens());
    }

    @Test
    public void parsesInputTransformations() throws Exception {
        String body = "{\"id\":\"msg_1\",\"stop_reason\":\"end_turn\","
                + "\"input_transformations\":[{\"type\":\"thinking_dropped\","
                + "\"path\":\"messages.3.content.0\",\"reason\":\"model_binding_mismatch\"}]}";

        ChatResponse response = MAPPER.readValue(body, ChatResponse.class);

        assertEquals(1, response.getInputTransformations().size());
        assertEquals("thinking_dropped", response.getInputTransformations().get(0).getType());
        assertEquals("messages.3.content.0", response.getInputTransformations().get(0).getPath());
        assertEquals("model_binding_mismatch", response.getInputTransformations().get(0).getReason());
    }

    @Test
    public void inputTransformationsAbsentWithoutTheBetaFlag() throws Exception {
        String body = "{\"id\":\"msg_1\",\"stop_reason\":\"end_turn\"}";
        ChatResponse response = MAPPER.readValue(body, ChatResponse.class);
        assertNull(response.getInputTransformations());
    }

    /**
     * vac-chat replays assistant history verbatim, including Opus 4.8 encrypted
     * thinking blocks whose text is empty and which carry only a signature.
     * The SDK must not drop, rewrite, or reorder any of that: 1.11.0 only added
     * optional request fields, it introduced no client-side validation.
     */
    @Test
    public void emptyThinkingBlocksWithSignaturesRoundTripUntouched() throws Exception {
        String original = "[" 
                + "{\"type\":\"thinking\",\"thinking\":\"\",\"signature\":\"sig-abc\"},"
                + "{\"type\":\"redacted_thinking\",\"data\":\"encrypted-payload\"},"
                + "{\"type\":\"text\",\"text\":\"answer\"}]";

        java.util.List<me.vacuity.ai.sdk.claude.entity.ChatMessageContent> blocks =
                MAPPER.readValue(original, MAPPER.getTypeFactory().constructCollectionType(
                        java.util.List.class,
                        me.vacuity.ai.sdk.claude.entity.ChatMessageContent.class));

        assertEquals(3, blocks.size(), "no block may be dropped");

        assertEquals("thinking", blocks.get(0).getType());
        assertEquals("", blocks.get(0).getThinking(), "empty thinking text must survive as empty, not null");
        assertEquals("sig-abc", blocks.get(0).getSignature());

        assertEquals("redacted_thinking", blocks.get(1).getType());
        assertEquals("encrypted-payload", blocks.get(1).getData());

        com.fasterxml.jackson.databind.JsonNode back = MAPPER.readTree(MAPPER.writeValueAsString(blocks));
        assertEquals(3, back.size());
        assertEquals("", back.get(0).get("thinking").asText());
        assertEquals("sig-abc", back.get(0).get("signature").asText());
        assertEquals("encrypted-payload", back.get(1).get("data").asText());
        assertEquals("answer", back.get(2).get("text").asText());
    }

    /**
     * block_binding is opt-in: a request that never sets it must not carry it,
     * so existing multi-turn callers keep the pre-1.11.0 wire shape.
     */
    @Test
    public void blockBindingIsAbsentUnlessAskedFor() throws Exception {
        me.vacuity.ai.sdk.claude.request.ChatRequest request =
                me.vacuity.ai.sdk.claude.request.ChatRequest.builder()
                        .model("claude-opus-5")
                        .maxTokens(16)
                        .thinking(me.vacuity.ai.sdk.claude.entity.Thinking.adaptive())
                        .build();

        com.fasterxml.jackson.databind.JsonNode json =
                MAPPER.readTree(MAPPER.writeValueAsString(request));

        assertFalse(json.get("thinking").has("block_binding"));
    }
}
