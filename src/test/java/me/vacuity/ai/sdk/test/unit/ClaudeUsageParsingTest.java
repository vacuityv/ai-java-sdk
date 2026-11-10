package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.vacuity.ai.sdk.claude.ClaudeClient;
import me.vacuity.ai.sdk.claude.entity.Usage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Claude's cache accounting, which is additive where the OpenAI-compatible
 * side is subtractive.
 * <p>
 * Both bodies are verbatim live claude-opus-5-5 responses from 2026-11-10: the
 * same request sent twice with a ~4.8k-token cached system prompt.
 *
 * @author vacuity
 */
public class ClaudeUsageParsingTest {

    private final ObjectMapper mapper = ClaudeClient.defaultObjectMapper();

    /** First call: the prompt is written to cache. */
    private static final String WRITING = "{\"input_tokens\":13,"
            + "\"cache_creation_input_tokens\":4763,\"cache_read_input_tokens\":0,"
            + "\"cache_creation\":{\"ephemeral_5m_input_tokens\":4763,"
            + "\"ephemeral_1h_input_tokens\":0},"
            + "\"output_tokens\":4,\"output_tokens_details\":{\"thinking_tokens\":0},"
            + "\"service_tier\":\"standard\",\"inference_geo\":\"global\","
            + "\"speed\":\"standard\"}";

    /** Second call: the prompt is read back from cache. */
    private static final String READING = "{\"input_tokens\":13,"
            + "\"cache_creation_input_tokens\":0,\"cache_read_input_tokens\":4763,"
            + "\"cache_creation\":{\"ephemeral_5m_input_tokens\":0,"
            + "\"ephemeral_1h_input_tokens\":0},"
            + "\"output_tokens\":4,\"service_tier\":\"standard\","
            + "\"inference_geo\":\"global\",\"speed\":\"standard\"}";

    @Test
    public void cacheCreationSplitsByLifetime() throws Exception {
        Usage u = mapper.readValue(WRITING, Usage.class);

        assertNotNull(u.getCacheCreation(), "cache_creation must deserialize");
        assertEquals(4763, u.getCacheCreation().getEphemeral5mInputTokens());
        assertEquals(0, u.getCacheCreation().getEphemeral1hInputTokens());
        // the split must account for the whole write: 5m costs 1.25x, 1h costs 2x
        assertEquals(u.getCacheCreationInputTokens(),
                u.getCacheCreation().getEphemeral5mInputTokens()
                        + u.getCacheCreation().getEphemeral1hInputTokens(),
                "the two lifetimes must add up to cache_creation_input_tokens");
    }

    @Test
    public void inferenceGeoAndSpeedParse() throws Exception {
        Usage u = mapper.readValue(WRITING, Usage.class);

        assertEquals("global", u.getInferenceGeo());
        assertEquals("standard", u.getSpeed());
    }

    /**
     * The whole point: inputTokens is the remainder, not the total. Billing it
     * as a total would charge 13 tokens for a 4776-token prompt.
     */
    @Test
    public void inputTokensIsTheRemainderSoTheTotalIsAdditive() throws Exception {
        Usage writing = mapper.readValue(WRITING, Usage.class);
        assertEquals(13, writing.getInputTokens());
        assertEquals(4776, total(writing));

        Usage reading = mapper.readValue(READING, Usage.class);
        assertEquals(13, reading.getInputTokens(),
                "a cache hit does not shrink inputTokens — it was never the total");
        assertEquals(4776, total(reading));

        // both calls saw the same prompt, and the sum says so even though the
        // buckets moved
        assertEquals(total(writing), total(reading));
    }

    @Test
    public void aResponseWithoutCachingCarriesNoCacheFields() throws Exception {
        Usage plain = mapper.readValue(
                "{\"input_tokens\":100,\"output_tokens\":5}", Usage.class);

        assertEquals(100, plain.getInputTokens(),
                "with no caching in play the remainder IS the whole prompt");
        assertNull(plain.getCacheCreation());
        assertNull(plain.getCacheCreationInputTokens());
        assertNull(plain.getCacheReadInputTokens());
    }

    /**
     * The addition the Claude side needs, done by the caller — the SDK does
     * not reconcile vendors' differing cache arithmetic.
     */
    private static int total(Usage u) {
        return nz(u.getInputTokens()) + nz(u.getCacheCreationInputTokens())
                + nz(u.getCacheReadInputTokens());
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }

    @Test
    public void unknownUsageFieldsDoNotFailDeserialization() throws Exception {
        Usage u = mapper.readValue(
                "{\"input_tokens\":1,\"some_future_field\":9,"
                        + "\"cache_creation\":{\"ephemeral_5m_input_tokens\":1,"
                        + "\"ephemeral_7d_input_tokens\":2}}", Usage.class);

        assertEquals(1, u.getInputTokens());
        assertEquals(1, u.getCacheCreation().getEphemeral5mInputTokens());
    }
}
