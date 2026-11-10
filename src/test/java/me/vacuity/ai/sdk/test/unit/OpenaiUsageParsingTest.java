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

    /**
     * Verbatim live gpt-5.6-terra response, FIRST of two identical calls:
     * the prompt is written to cache, nothing is read from it.
     */
    private static final String OPENAI_WRITING = "{\"prompt_tokens\":2655,"
            + "\"completion_tokens\":4,\"total_tokens\":2659,"
            + "\"prompt_tokens_details\":{\"cached_tokens\":0,"
            + "\"cache_write_tokens\":2652,\"audio_tokens\":0},"
            + "\"completion_tokens_details\":{\"reasoning_tokens\":0,\"audio_tokens\":0,"
            + "\"accepted_prediction_tokens\":0,\"rejected_prediction_tokens\":0}}";

    /** Verbatim live gpt-5.6-terra response, SECOND call: a full cache read. */
    private static final String OPENAI_READING = "{\"prompt_tokens\":2655,"
            + "\"completion_tokens\":4,\"total_tokens\":2659,"
            + "\"prompt_tokens_details\":{\"cached_tokens\":2652,"
            + "\"cache_write_tokens\":0,\"audio_tokens\":0}}";

    /**
     * Bailian's OpenAI-compatible shape, from its context-cache page. Not
     * live-verified: no Bailian credentials on this machine.
     */
    private static final String BAILIAN = "{\"prompt_tokens\":2000,"
            + "\"completion_tokens\":10,\"total_tokens\":2010,"
            + "\"prompt_tokens_details\":{\"cached_tokens\":0,"
            + "\"cache_creation_input_tokens\":1536}}";

    /** The example from DeepSeek's create-chat-completion reference. */
    private static final String DEEPSEEK = "{\"prompt_tokens\":7,\"completion_tokens\":11,"
            + "\"total_tokens\":18,\"prompt_tokens_details\":{\"cached_tokens\":0},"
            + "\"prompt_cache_hit_tokens\":0,\"prompt_cache_miss_tokens\":7}";

    /**
     * A real deepseek-v4-flash response with an actual cache hit: streaming
     * with include_usage, the same prefix sent twice, second call.
     * <p>
     * Provenance matters here. The docs example above has a zero hit count, so
     * it cannot show that a non-zero hit parses or that the arithmetic holds.
     * These numbers come from a run by the Firefly maintainer, reported
     * 2026-11-10 — not reproduced on this machine, which has no DeepSeek
     * credentials and reaches no DeepSeek model through its proxy. They are
     * better evidence than the docs example and weaker than something this
     * suite could re-run.
     */
    private static final String DEEPSEEK_HIT = "{\"prompt_tokens\":1832,"
            + "\"prompt_tokens_details\":{\"cached_tokens\":1664},"
            + "\"prompt_cache_hit_tokens\":1664,\"prompt_cache_miss_tokens\":168}";

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
        assertEquals(1792, chunk.getUsage().getPromptTokensDetails().getCachedTokens());
    }


    // -------------------------------------------- 缓存写入 token (2.2.2)








    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }

    // ------------------------------- 各家字段的原样映射与算术事实 (2.3.0)

    /**
     * OpenAI's own shape, including the cache-write field that gpt-5.6 and
     * later add. The arithmetic is checked here rather than in the SDK: the
     * SDK reports each vendor's fields as that vendor defines them and does
     * not reconcile them.
     */
    @Test
    public void openaiBucketsAreDisjointSubsetsOfPromptTokens() throws Exception {
        Usage writing = mapper.readValue(OPENAI_WRITING, Usage.class);
        assertEquals(2652, writing.getPromptTokensDetails().getCacheWriteTokens());
        assertEquals(0, writing.getPromptTokensDetails().getCachedTokens());
        assertEquals(2655, writing.getPromptTokens());
        // 3 ordinary + 2652 written + 0 cached
        assertEquals(writing.getPromptTokens(),
                3 + writing.getPromptTokensDetails().getCacheWriteTokens()
                        + writing.getPromptTokensDetails().getCachedTokens());

        Usage reading = mapper.readValue(OPENAI_READING, Usage.class);
        assertEquals(0, reading.getPromptTokensDetails().getCacheWriteTokens());
        assertEquals(2652, reading.getPromptTokensDetails().getCachedTokens());
        assertEquals(reading.getPromptTokens(),
                3 + reading.getPromptTokensDetails().getCacheWriteTokens()
                        + reading.getPromptTokensDetails().getCachedTokens());
    }

    /**
     * gpt-5.5 predates write accounting: the field is absent, which must read
     * as null rather than 0 — "this model does not report it" and "it was zero
     * this time" are different facts.
     */
    @Test
    public void olderOpenaiModelsReportNoWriteField() throws Exception {
        Usage u = mapper.readValue(OPENAI_CACHED, Usage.class);

        assertNull(u.getPromptTokensDetails().getCacheWriteTokens());
        assertEquals(1792, u.getPromptTokensDetails().getCachedTokens());
    }

    /**
     * Bailian's own name for the write count, from its context-cache page.
     * Documented, not live-verified: no Bailian credentials here.
     */
    @Test
    public void bailianWriteFieldParsesUnderItsOwnName() throws Exception {
        Usage u = mapper.readValue(BAILIAN, Usage.class);

        assertEquals(1536, u.getPromptTokensDetails().getCacheCreationInputTokens());
        assertEquals(0, u.getPromptTokensDetails().getCachedTokens());
        // OpenAI's name is absent here — the two are not merged
        assertNull(u.getPromptTokensDetails().getCacheWriteTokens());
    }

    /**
     * DeepSeek documents prompt_tokens = hit + miss, and fills the standard
     * shape with the same hit count. Both are carried, unreconciled.
     */
    @Test
    public void deepseekReportsBothShapesAndNoWriteField() throws Exception {
        Usage u = mapper.readValue(DEEPSEEK, Usage.class);

        assertEquals(u.getPromptTokens(),
                u.getPromptCacheHitTokens() + u.getPromptCacheMissTokens());
        assertEquals(u.getPromptCacheHitTokens(),
                u.getPromptTokensDetails().getCachedTokens(),
                "DeepSeek's docs call cached_tokens the same as prompt_cache_hit_tokens");
        assertNull(u.getPromptTokensDetails().getCacheWriteTokens(),
                "DeepSeek documents no write accounting");
    }

    /**
     * The non-zero cache hit the docs example cannot show: both shapes carry
     * the same number, and hit + miss accounts for the whole prompt.
     */
    @Test
    public void deepseekRealCacheHitParsesInBothShapes() throws Exception {
        Usage u = mapper.readValue(DEEPSEEK_HIT, Usage.class);

        assertEquals(1664, u.getPromptCacheHitTokens());
        assertEquals(168, u.getPromptCacheMissTokens());
        assertEquals(1664, u.getPromptTokensDetails().getCachedTokens(),
                "DeepSeek fills the standard shape with the same hit count");
        assertEquals(1832, u.getPromptTokens());

        // the documented identity, now against a response that actually hit
        assertEquals(u.getPromptTokens(),
                u.getPromptCacheHitTokens() + u.getPromptCacheMissTokens(),
                "prompt_tokens = hit + miss");

        // and no write accounting, as DeepSeek's docs say
        assertNull(u.getPromptTokensDetails().getCacheWriteTokens());
        assertNull(u.getPromptTokensDetails().getCacheCreationInputTokens());
    }
}
