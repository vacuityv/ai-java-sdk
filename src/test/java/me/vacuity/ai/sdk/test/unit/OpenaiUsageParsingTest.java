package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.entity.Usage;
import me.vacuity.ai.sdk.openai.response.ChatResponse;
import me.vacuity.ai.sdk.openai.response.StreamChatResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Parses the cache-token fields off the OpenAI-compatible usage object.
 * <p>
 * The bodies below are not invented. The OpenAI one is a verbatim response
 * from a live call made on 2026-11-10 (the same prompt sent twice, so the
 * second shows a real cache hit); the DeepSeek one is the example from
 * DeepSeek's own API reference. Vendors report cache hits in two different
 * shapes and anything billing from this has to read both.
 *
 * @author vacuity
 */
public class OpenaiUsageParsingTest {

    private final ObjectMapper mapper = OpenaiClient.defaultObjectMapper();

    /** Verbatim live response, second of two identical calls. */
    private static final String OPENAI_CACHED = "{\"prompt_tokens\":2658,"
            + "\"completion_tokens\":16,\"total_tokens\":2674,"
            + "\"prompt_tokens_details\":{\"cached_tokens\":1792,\"audio_tokens\":0},"
            + "\"completion_tokens_details\":{\"reasoning_tokens\":16,\"audio_tokens\":0,"
            + "\"accepted_prediction_tokens\":0,\"rejected_prediction_tokens\":0}}";

    /** The example from DeepSeek's create-chat-completion reference. */
    private static final String DEEPSEEK = "{\"prompt_tokens\":7,\"completion_tokens\":11,"
            + "\"total_tokens\":18,\"prompt_tokens_details\":{\"cached_tokens\":0},"
            + "\"prompt_cache_hit_tokens\":0,\"prompt_cache_miss_tokens\":7}";

    @Test
    public void openaiShapeParses() throws Exception {
        Usage usage = mapper.readValue(OPENAI_CACHED, Usage.class);

        assertEquals(2658, usage.getPromptTokens());
        assertNotNull(usage.getPromptTokensDetails(), "prompt_tokens_details must deserialize");
        assertEquals(1792, usage.getPromptTokensDetails().getCachedTokens());
        assertEquals(0, usage.getPromptTokensDetails().getAudioTokens());

        assertNotNull(usage.getCompletionTokensDetails());
        assertEquals(16, usage.getCompletionTokensDetails().getReasoningTokens());
        assertEquals(0, usage.getCompletionTokensDetails().getAcceptedPredictionTokens());
        assertEquals(0, usage.getCompletionTokensDetails().getRejectedPredictionTokens());

        // OpenAI does not use DeepSeek's flat shape
        assertNull(usage.getPromptCacheHitTokens());
        assertNull(usage.getPromptCacheMissTokens());
    }

    @Test
    public void deepseekShapeParses() throws Exception {
        Usage usage = mapper.readValue(DEEPSEEK, Usage.class);

        assertEquals(0, usage.getPromptCacheHitTokens());
        assertEquals(7, usage.getPromptCacheMissTokens());
        // DeepSeek fills the standard shape too, with the same number
        assertEquals(0, usage.getPromptTokensDetails().getCachedTokens());
        // and documents prompt_tokens = hit + miss
        assertEquals(usage.getPromptTokens(),
                usage.getPromptCacheHitTokens() + usage.getPromptCacheMissTokens(),
                "DeepSeek documents prompt_tokens = hit + miss");
    }

    /**
     * The whole point of the release: one accessor that bills correctly
     * whichever shape arrived.
     */
    @Test
    public void cachedPromptTokensReadsEitherShape() throws Exception {
        assertEquals(1792, mapper.readValue(OPENAI_CACHED, Usage.class).getCachedPromptTokens());

        // only DeepSeek's flat fields, no details block
        Usage flatOnly = mapper.readValue(
                "{\"prompt_tokens\":100,\"prompt_cache_hit_tokens\":60,"
                        + "\"prompt_cache_miss_tokens\":40}", Usage.class);
        assertEquals(60, flatOnly.getCachedPromptTokens(),
                "must fall back to prompt_cache_hit_tokens when there is no details block");

        // a vendor that reports no caching at all
        Usage none = mapper.readValue(
                "{\"prompt_tokens\":100,\"completion_tokens\":5,\"total_tokens\":105}",
                Usage.class);
        assertNull(none.getCachedPromptTokens(), "no cache information means null, not 0");
    }

    /**
     * Cached tokens are a subset of prompt_tokens, so the full-rate part is
     * the difference. Billing prompt_tokens AND the cached count charges the
     * cached part twice.
     */
    @Test
    public void uncachedPromptTokensIsTheFullRatePortion() throws Exception {
        Usage openai = mapper.readValue(OPENAI_CACHED, Usage.class);
        assertEquals(2658 - 1792, openai.getUncachedPromptTokens());
        assertEquals(openai.getPromptTokens(),
                openai.getCachedPromptTokens() + openai.getUncachedPromptTokens(),
                "the two parts must add back up to prompt_tokens");

        Usage deepseek = mapper.readValue(DEEPSEEK, Usage.class);
        assertEquals(7, deepseek.getUncachedPromptTokens());
        assertEquals(deepseek.getPromptCacheMissTokens(), deepseek.getUncachedPromptTokens(),
                "for DeepSeek the derived value must agree with its own miss count");

        // unknown split: charge the whole prompt rather than silently under-billing
        Usage none = mapper.readValue("{\"prompt_tokens\":100}", Usage.class);
        assertEquals(100, none.getUncachedPromptTokens());

        // nothing to go on at all
        assertNull(mapper.readValue("{}", Usage.class).getUncachedPromptTokens());
    }

    @Test
    public void unknownUsageFieldsDoNotFailDeserialization() throws Exception {
        Usage usage = mapper.readValue(
                "{\"prompt_tokens\":1,\"some_future_field\":123,"
                        + "\"prompt_tokens_details\":{\"cached_tokens\":1,\"future_detail\":9}}",
                Usage.class);

        assertEquals(1, usage.getPromptTokens());
        assertEquals(1, usage.getPromptTokensDetails().getCachedTokens());
    }

    @Test
    public void chatResponseCarriesTheCacheFields() throws Exception {
        ChatResponse response = mapper.readValue(
                "{\"id\":\"chatcmpl-1\",\"model\":\"deepseek-flash\","
                        + "\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\","
                        + "\"content\":\"ok\"},\"finish_reason\":\"stop\"}],"
                        + "\"usage\":" + DEEPSEEK + "}", ChatResponse.class);

        assertEquals(0, response.getUsage().getPromptCacheHitTokens());
        assertEquals(7, response.getUsage().getPromptCacheMissTokens());
    }

    /**
     * With includeUsage set, OpenAI sends {@code "usage": null} on every chunk
     * but the last. An explicit null is not the same as an absent field, and
     * it must not blow up mid-stream.
     */
    @Test
    public void streamChunkWithExplicitNullUsageParses() throws Exception {
        StreamChatResponse chunk = mapper.readValue(
                "{\"id\":\"chatcmpl-1\",\"choices\":[{\"index\":0,"
                        + "\"delta\":{\"content\":\"ok\"}}],\"usage\":null}",
                StreamChatResponse.class);

        assertNull(chunk.getUsage(), "an explicit null usage must parse as null");
    }

    @Test
    public void finalStreamChunkCarriesUsage() throws Exception {
        StreamChatResponse chunk = mapper.readValue(
                "{\"id\":\"chatcmpl-1\",\"choices\":[],\"usage\":" + OPENAI_CACHED + "}",
                StreamChatResponse.class);

        assertNotNull(chunk.getUsage());
        assertEquals(1792, chunk.getUsage().getCachedPromptTokens());
    }
}
