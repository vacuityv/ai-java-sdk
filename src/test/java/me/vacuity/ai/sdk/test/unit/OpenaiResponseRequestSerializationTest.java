package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.responses.request.ContextManagement;
import me.vacuity.ai.sdk.openai.responses.request.PromptCacheOptions;
import me.vacuity.ai.sdk.openai.responses.request.ResponseModeration;
import me.vacuity.ai.sdk.openai.responses.request.ResponsePrompt;
import me.vacuity.ai.sdk.openai.responses.request.ResponseReasoning;
import me.vacuity.ai.sdk.openai.responses.request.ResponseRequest;
import me.vacuity.ai.sdk.openai.responses.request.ResponseStreamOptions;
import me.vacuity.ai.sdk.openai.responses.request.ResponseTextConfig;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Wire-format tests for the parameters added to the OpenAI Responses API
 * request object.
 */
public class OpenaiResponseRequestSerializationTest {

    private static final ObjectMapper MAPPER = OpenaiClient.defaultObjectMapper();

    private JsonNode serialize(ResponseRequest request) throws Exception {
        return MAPPER.readTree(MAPPER.writeValueAsString(request));
    }

    private ResponseRequest.ResponseRequestBuilder minimal() {
        return ResponseRequest.builder().model("gpt-5.2").input("hi");
    }

    @Test
    public void promptCachingParametersUseSnakeCaseKeys() throws Exception {
        JsonNode json = serialize(minimal()
                .promptCacheKey("cache-key-1")
                .promptCacheRetention("24h")
                .promptCacheOptions(PromptCacheOptions.builder().mode("explicit").ttl("30m").build())
                .build());

        assertEquals("cache-key-1", json.get("prompt_cache_key").asText());
        assertEquals("24h", json.get("prompt_cache_retention").asText());
        assertEquals("explicit", json.get("prompt_cache_options").get("mode").asText());
        assertEquals("30m", json.get("prompt_cache_options").get("ttl").asText());
    }

    @Test
    public void safetyIdentifierReplacesDeprecatedUser() throws Exception {
        JsonNode json = serialize(minimal().safetyIdentifier("user-abc").build());
        assertEquals("user-abc", json.get("safety_identifier").asText());
        assertFalse(json.has("user"));
    }

    @Test
    public void conversationAcceptsAnIdString() throws Exception {
        JsonNode json = serialize(minimal().conversation("conv_123").build());
        assertEquals("conv_123", json.get("conversation").asText());
    }

    @Test
    public void promptTemplateReferenceSerializes() throws Exception {
        JsonNode json = serialize(minimal()
                .prompt(ResponsePrompt.builder()
                        .id("pmpt_1")
                        .version("2")
                        .variables(Collections.singletonMap("city", "Paris"))
                        .build())
                .build());

        assertEquals("pmpt_1", json.get("prompt").get("id").asText());
        assertEquals("2", json.get("prompt").get("version").asText());
        assertEquals("Paris", json.get("prompt").get("variables").get("city").asText());
    }

    @Test
    public void contextManagementIsAnArrayOfEntries() throws Exception {
        JsonNode json = serialize(minimal()
                .contextManagement(Collections.singletonList(
                        ContextManagement.builder().type("compaction").compactThreshold(50000).build()))
                .build());

        JsonNode first = json.get("context_management").get(0);
        assertTrue(json.get("context_management").isArray());
        assertEquals("compaction", first.get("type").asText());
        assertEquals(50000, first.get("compact_threshold").asInt());
    }

    @Test
    public void moderationNestsPolicyModes() throws Exception {
        JsonNode json = serialize(minimal()
                .moderation(ResponseModeration.builder()
                        .model("omni-moderation-latest")
                        .policy(ResponseModeration.Policy.builder()
                                .input(ResponseModeration.Mode.builder().mode("block").build())
                                .output(ResponseModeration.Mode.builder().mode("score").build())
                                .build())
                        .build())
                .build());

        JsonNode moderation = json.get("moderation");
        assertEquals("omni-moderation-latest", moderation.get("model").asText());
        assertEquals("block", moderation.get("policy").get("input").get("mode").asText());
        assertEquals("score", moderation.get("policy").get("output").get("mode").asText());
    }

    @Test
    public void streamOptionsCarriesIncludeObfuscation() throws Exception {
        JsonNode json = serialize(minimal()
                .streamOptions(ResponseStreamOptions.builder().includeObfuscation(false).build())
                .build());
        assertFalse(json.get("stream_options").get("include_obfuscation").asBoolean());
    }

    @Test
    public void textConfigCarriesVerbosity() throws Exception {
        JsonNode json = serialize(minimal()
                .text(ResponseTextConfig.builder().verbosity("low").build())
                .build());
        assertEquals("low", json.get("text").get("verbosity").asText());
    }

    @Test
    public void reasoningCarriesContextAndMode() throws Exception {
        JsonNode json = serialize(minimal()
                .reasoning(ResponseReasoning.builder()
                        .effort("xhigh")
                        .summary("auto")
                        .context("all_turns")
                        .mode("pro")
                        .build())
                .build());

        JsonNode reasoning = json.get("reasoning");
        assertEquals("xhigh", reasoning.get("effort").asText());
        assertEquals("auto", reasoning.get("summary").asText());
        assertEquals("all_turns", reasoning.get("context").asText());
        assertEquals("pro", reasoning.get("mode").asText());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void deprecatedReasoningEffortIsNoLongerSerialized() throws Exception {
        // reasoning_effort is a Chat Completions top-level parameter and is not
        // part of the Responses reasoning object — sending it is an unknown field.
        ResponseReasoning reasoning = ResponseReasoning.builder().effort("high").build();
        reasoning.setReasoningEffort("SHOULD-NOT-APPEAR");

        JsonNode json = serialize(minimal().reasoning(reasoning).build());

        assertFalse(json.get("reasoning").has("reasoning_effort"));
        assertEquals("high", json.get("reasoning").get("effort").asText());
    }

    @Test
    public void unsetOptionalsAreOmitted() throws Exception {
        JsonNode json = serialize(minimal().build());
        assertFalse(json.has("moderation"));
        assertFalse(json.has("conversation"));
        assertFalse(json.has("prompt_cache_options"));
        assertFalse(json.has("stream_options"));
        assertFalse(json.has("context_management"));
    }
}
