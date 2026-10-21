package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.vacuity.ai.sdk.gemini.GeminiClient;
import me.vacuity.ai.sdk.gemini.entity.Usage;
import me.vacuity.ai.sdk.gemini.response.ChatResponse;
import me.vacuity.ai.sdk.gemini.response.StreamChatResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Parsing tests for Gemini usageMetadata.
 * <p>
 * The fixtures are captured verbatim from real gemini-4-argon traffic, so the
 * field names are pinned to what the API actually sends rather than to the
 * docs.
 */
public class GeminiUsageParsingTest {

    private static final ObjectMapper MAPPER = GeminiClient.defaultObjectMapper();

    /** The final chunk of a real thinking stream. */
    private static final String REAL_FINAL_USAGE =
            "{\"promptTokenCount\":60,\"candidatesTokenCount\":771,\"totalTokenCount\":1966,"
            + "\"promptTokensDetails\":[{\"modality\":\"TEXT\",\"tokenCount\":60}],"
            + "\"candidatesTokensDetails\":[{\"modality\":\"TEXT\",\"tokenCount\":771}],"
            + "\"thoughtsTokenCount\":1135}";

    @Test
    public void thoughtsTokenCountParses() throws Exception {
        Usage usage = MAPPER.readValue(REAL_FINAL_USAGE, Usage.class);

        assertEquals(1135, usage.getThoughtsTokenCount());
        assertEquals(771, usage.getCandidatesTokenCount());
        assertEquals(60, usage.getPromptTokenCount());
        assertEquals(1966, usage.getTotalTokenCount());
    }

    @Test
    public void modalityBreakdownsParse() throws Exception {
        Usage usage = MAPPER.readValue(REAL_FINAL_USAGE, Usage.class);

        assertEquals(1, usage.getPromptTokensDetails().size());
        assertEquals("TEXT", usage.getPromptTokensDetails().get(0).getModality());
        assertEquals(60, usage.getPromptTokensDetails().get(0).getTokenCount());
        assertEquals(771, usage.getCandidatesTokensDetails().get(0).getTokenCount());
    }

    /**
     * thoughtsTokenCount is excluded from candidatesTokenCount but billed at the
     * output rate, so billing on candidatesTokenCount alone undercounts.
     */
    @Test
    public void billableOutputAddsThinkingToTheVisibleAnswer() throws Exception {
        Usage usage = MAPPER.readValue(REAL_FINAL_USAGE, Usage.class);
        assertEquals(771 + 1135, usage.getBillableOutputTokenCount());
    }

    @Test
    public void billableOutputToleratesMissingCounters() {
        Usage none = Usage.builder().build();
        assertNull(none.getBillableOutputTokenCount());

        Usage noThinking = Usage.builder().candidatesTokenCount(100).build();
        assertEquals(100, noThinking.getBillableOutputTokenCount());

        Usage thinkingOnly = Usage.builder().thoughtsTokenCount(50).build();
        assertEquals(50, thinkingOnly.getBillableOutputTokenCount());
    }

    @Test
    public void absentFieldsStayNull() throws Exception {
        Usage usage = MAPPER.readValue(
                "{\"promptTokenCount\":10,\"candidatesTokenCount\":5,\"totalTokenCount\":15}",
                Usage.class);

        assertNull(usage.getThoughtsTokenCount());
        assertNull(usage.getCachedContentTokenCount());
        assertNull(usage.getToolUsePromptTokenCount());
        assertNull(usage.getPromptTokensDetails());
        assertNull(usage.getTrafficType());
    }

    @Test
    public void cacheAndToolFieldsParse() throws Exception {
        Usage usage = MAPPER.readValue(
                "{\"cachedContentTokenCount\":128,\"toolUsePromptTokenCount\":64,"
                + "\"cacheTokensDetails\":[{\"modality\":\"TEXT\",\"tokenCount\":128}],"
                + "\"toolUsePromptTokensDetails\":[{\"modality\":\"TEXT\",\"tokenCount\":64}],"
                + "\"trafficType\":\"ON_DEMAND\"}", Usage.class);

        assertEquals(128, usage.getCachedContentTokenCount());
        assertEquals(64, usage.getToolUsePromptTokenCount());
        assertEquals(128, usage.getCacheTokensDetails().get(0).getTokenCount());
        assertEquals(64, usage.getToolUsePromptTokensDetails().get(0).getTokenCount());
        assertEquals("ON_DEMAND", usage.getTrafficType());
    }

    @Test
    public void futureFieldsAreKeptRatherThanDropped() throws Exception {
        Usage usage = MAPPER.readValue(
                "{\"promptTokenCount\":1,\"someCounterGoogleAddsLater\":42}", Usage.class);

        assertEquals(42, usage.getExtra().get("someCounterGoogleAddsLater"));
    }

    @Test
    public void nonStreamingResponseExposesUsage() throws Exception {
        ChatResponse response = MAPPER.readValue(
                "{\"candidates\":[],\"usageMetadata\":" + REAL_FINAL_USAGE + "}",
                ChatResponse.class);

        assertEquals(1135, response.getUsageMetadata().getThoughtsTokenCount());
    }

    @Test
    public void streamingResponseExposesTheSameUsageType() throws Exception {
        StreamChatResponse chunk = MAPPER.readValue(
                "{\"candidates\":[],\"usageMetadata\":" + REAL_FINAL_USAGE + "}",
                StreamChatResponse.class);

        assertEquals(1135, chunk.getUsageMetadata().getThoughtsTokenCount());
        assertEquals(771 + 1135, chunk.getUsageMetadata().getBillableOutputTokenCount());
    }

    /**
     * Streaming counters are cumulative, not per-chunk deltas. An early chunk
     * carries thoughts but no candidates yet; summing chunks would massively
     * overcount.
     */
    @Test
    public void earlyChunkCarriesThoughtsButNoAnswerYet() throws Exception {
        StreamChatResponse early = MAPPER.readValue(
                "{\"usageMetadata\":{\"promptTokenCount\":29,\"thoughtsTokenCount\":129,"
                + "\"totalTokenCount\":158}}", StreamChatResponse.class);

        assertEquals(129, early.getUsageMetadata().getThoughtsTokenCount());
        assertNull(early.getUsageMetadata().getCandidatesTokenCount(),
                "candidatesTokenCount only appears once the answer starts");
        assertEquals(129, early.getUsageMetadata().getBillableOutputTokenCount());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void legacyThreeArgConstructorStillCompiles() {
        Usage usage = new Usage(10, 20, 30);
        assertEquals(10, usage.getPromptTokenCount());
        assertEquals(20, usage.getCandidatesTokenCount());
        assertEquals(30, usage.getTotalTokenCount());
        assertNotNull(usage.getExtra());
        assertTrue(usage.getExtra().isEmpty());
    }
}
