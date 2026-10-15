package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.vacuity.ai.sdk.gemini.GeminiClient;
import me.vacuity.ai.sdk.gemini.entity.ChatMessage;
import me.vacuity.ai.sdk.gemini.entity.ChatMessageContentPart;
import me.vacuity.ai.sdk.gemini.entity.ToolConfig;
import me.vacuity.ai.sdk.gemini.request.ChatRequest;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Wire-format tests for Gemini's toolConfig and for the server-side tool parts
 * that appear once includeServerSideToolInvocations is on.
 */
public class GeminiToolConfigSerializationTest {

    private static final ObjectMapper MAPPER = GeminiClient.defaultObjectMapper();

    private JsonNode serialize(Object o) throws Exception {
        return MAPPER.readTree(MAPPER.writeValueAsString(o));
    }

    private ChatRequest.ChatRequestBuilder minimal() {
        return ChatRequest.builder()
                .model("gemini-4-argon")
                .contents(Collections.singletonList(
                        ChatMessage.builder().role("user").build()));
    }

    @Test
    public void toolConfigUsesTheDocumentedKeyNames() throws Exception {
        JsonNode json = serialize(minimal()
                .toolConfig(ToolConfig.includeServerSideToolInvocations())
                .build());

        assertTrue(json.get("toolConfig").get("includeServerSideToolInvocations").asBoolean());
    }

    @Test
    public void toolConfigIsOmittedWhenUnset() throws Exception {
        // Older models were never tested with a stray field, so an unset
        // toolConfig must not be serialized at all — not even as null.
        JsonNode json = serialize(minimal().build());
        assertFalse(json.has("toolConfig"));
    }

    @Test
    public void toolConfigOmitsTheFlagWhenOnlyFunctionCallingConfigIsSet() throws Exception {
        JsonNode json = serialize(minimal()
                .toolConfig(ToolConfig.builder().build())
                .build());

        assertTrue(json.has("toolConfig"));
        assertFalse(json.get("toolConfig").has("includeServerSideToolInvocations"));
        assertFalse(json.get("toolConfig").has("functionCallingConfig"));
    }

    @Test
    public void modelStaysOutOfTheRequestBody() throws Exception {
        // model is a path parameter; serializing it would be a regression.
        JsonNode json = serialize(minimal()
                .toolConfig(ToolConfig.includeServerSideToolInvocations())
                .build());
        assertFalse(json.has("model"));
    }

    @Test
    public void serverToolPartsParseFromTheRealResponseShape() throws Exception {
        String part = "{\"thoughtSignature\":\"EvsBCvgB\","
                + "\"toolCall\":{\"toolType\":\"GOOGLE_SEARCH_WEB\","
                + "\"args\":{\"queries\":[\"USD to JPY\"]},\"id\":\"call_53765\"}}";

        ChatMessageContentPart parsed = MAPPER.readValue(part, ChatMessageContentPart.class);

        assertNotNull(parsed.getToolCall());
        assertEquals("GOOGLE_SEARCH_WEB", parsed.getToolCall().getToolType());
        assertEquals("call_53765", parsed.getToolCall().getId());
        assertEquals("EvsBCvgB", parsed.getThoughtSignature());
    }

    @Test
    public void toolResponsePartParses() throws Exception {
        String part = "{\"thoughtSignature\":\"Euei\","
                + "\"toolResponse\":{\"toolType\":\"GOOGLE_SEARCH_WEB\","
                + "\"response\":{\"search_suggestions\":\"<style>x</style>\"}}}";

        ChatMessageContentPart parsed = MAPPER.readValue(part, ChatMessageContentPart.class);

        assertEquals("GOOGLE_SEARCH_WEB", parsed.getToolResponse().getToolType());
        assertEquals("<style>x</style>",
                parsed.getToolResponse().getResponse().get("search_suggestions"));
    }

    /**
     * The round-trip guarantee multi-turn Gemini 3/4 depends on: a part that
     * comes back with fields this SDK has never heard of must survive being
     * sent straight back.
     */
    @Test
    public void unmodelledPartFieldsSurviveARoundTrip() throws Exception {
        String original = "{\"text\":\"hi\",\"thoughtSignature\":\"sig\","
                + "\"someFieldShippedNextQuarter\":{\"nested\":[1,2]}}";

        ChatMessageContentPart parsed = MAPPER.readValue(original, ChatMessageContentPart.class);
        JsonNode reserialized = serialize(parsed);

        assertEquals("hi", reserialized.get("text").asText());
        assertEquals("sig", reserialized.get("thoughtSignature").asText());
        assertNotNull(reserialized.get("someFieldShippedNextQuarter"),
                "an unmodelled field must not be dropped on the way back out");
        assertEquals(2, reserialized.get("someFieldShippedNextQuarter").get("nested").get(1).asInt());
    }

    @Test
    public void knownFieldsAreNotDuplicatedIntoExtra() throws Exception {
        String original = "{\"text\":\"hi\",\"thoughtSignature\":\"sig\"}";
        ChatMessageContentPart parsed = MAPPER.readValue(original, ChatMessageContentPart.class);
        assertTrue(parsed.getExtra().isEmpty(),
                "modelled fields must not also land in the catch-all, got " + parsed.getExtra());
    }
}
