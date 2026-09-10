package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.vacuity.ai.sdk.claude.ClaudeClient;
import me.vacuity.ai.sdk.claude.response.ChatResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
