package me.vacuity.ai.sdk.test.unit;

import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.entity.ChatMessage;
import me.vacuity.ai.sdk.openai.entity.StreamOptions;
import me.vacuity.ai.sdk.openai.request.ChatRequest;
import me.vacuity.ai.sdk.openai.response.ChatResponse;
import me.vacuity.ai.sdk.openai.response.StreamChatResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves the cache-token fields are really populated by a live vendor, not
 * just parseable in a fixture. Skipped unless VAC_OPENAI_KEY is set.
 * <p>
 * Only the OpenAI shape is covered here. The DeepSeek shape
 * ({@code prompt_cache_hit_tokens} / {@code prompt_cache_miss_tokens}) has no
 * live coverage because this machine has no DeepSeek credentials and the
 * configured proxy serves no DeepSeek model — those fields were implemented
 * from DeepSeek's own API reference and are covered offline in
 * {@link OpenaiUsageParsingTest} by the example from that reference.
 *
 * @author vacuity
 */
@EnabledIfEnvironmentVariable(named = "VAC_OPENAI_KEY", matches = ".+")
public class OpenaiUsageLiveSmokeTest {

    private static final String MODEL = "gpt-5.5";

    /**
     * Cache writes and prompt_cache_options need a newer model: gpt-5.5
     * answers {@code prompt_cache_options is not supported on this model},
     * and reports no cache_write_tokens at all.
     */
    private static final String CACHE_MODEL = "gpt-5.6-terra";

    /**
     * Caching needs a prompt past the vendor's minimum, so this is
     * deliberately long and byte-identical between the two calls — a cache hit
     * requires the prefix to match exactly.
     */
    private static final String LONG_SYSTEM = repeat(
            "You are a meticulous code reviewer for a Java SDK. "
                    + "Follow these rules exactly and never deviate from them. ", 120);

    private OpenaiClient client() {
        String baseUrl = System.getenv("VAC_OPENAI_BASE_URL");
        String key = System.getenv("VAC_OPENAI_KEY");
        return baseUrl == null || baseUrl.isEmpty()
                ? new OpenaiClient(key, Duration.ofSeconds(180))
                : new OpenaiClient(key, Duration.ofSeconds(180), baseUrl);
    }

    private ChatRequest request() {
        return ChatRequest.builder()
                .model(MODEL)
                .messages(Arrays.asList(
                        ChatMessage.builder().role("system").content(LONG_SYSTEM).build(),
                        ChatMessage.builder().role("user").content("Reply with: ok").build()))
                .build();
    }

    /**
     * The same long prompt twice: the second call should report cached tokens.
     * <p>
     * Caching is best-effort on every vendor that offers it, so a miss on the
     * second call is not necessarily a bug. What this test refuses to tolerate
     * is the breakdown never arriving at all, which is what a wrong wire name
     * would look like.
     */
    @Test
    public void promptTokensDetailsArrivesAndReportsACacheHitOnTheSecondCall() {
        ChatResponse first = client().chat(request());
        assertNotNull(first.getUsage(), "a blocking call must report usage");
        assertNotNull(first.getUsage().getPromptTokensDetails(),
                "prompt_tokens_details must deserialize — a wrong wire name shows up here");

        ChatResponse second = client().chat(request());
        assertNotNull(second.getUsage().getPromptTokensDetails());
        Integer cached = second.getUsage().getPromptTokensDetails().getCachedTokens();
        assertNotNull(cached, "the second call must still report a cached-token count");

        assertTrue(cached > 0,
                "the second identical call should hit the cache; got " + cached
                        + " cached of " + second.getUsage().getPromptTokens()
                        + " prompt tokens. Caching is best-effort, so an occasional 0 is "
                        + "the vendor's call, not an SDK fault");

        // the containment the billing code depends on — checked here, not
        // encoded in the SDK
        assertTrue(cached <= second.getUsage().getPromptTokens(),
                "cached tokens are a subset of prompt_tokens, never more");
    }

    /**
     * Streaming reports usage only when asked. Without includeUsage, OpenAI
     * sends no usage on any chunk — not even the last — so anything billing
     * from a stream has to set this.
     */
    @Test
    public void streamingReportsUsageOnlyWhenIncludeUsageIsSet() {
        List<StreamChatResponse> without = collect(request());
        assertFalse(without.isEmpty(), "streaming produced no chunks");
        assertTrue(without.stream().allMatch(c -> c.getUsage() == null),
                "without includeUsage, no chunk should carry usage");

        ChatRequest asked = request();
        asked.setStreamOptions(StreamOptions.builder().includeUsage(true).build());
        List<StreamChatResponse> with = collect(asked);

        List<StreamChatResponse> carrying = new ArrayList<>();
        for (StreamChatResponse chunk : with) {
            if (chunk.getUsage() != null) {
                carrying.add(chunk);
            }
        }
        assertEquals(1, carrying.size(),
                "exactly one chunk should carry usage, got " + carrying.size()
                        + " of " + with.size() + " chunks");
        assertNotNull(carrying.get(0).getUsage().getPromptTokensDetails(),
                "the streamed usage must carry the prompt breakdown too");
        assertNotNull(carrying.get(0).getUsage().getPromptTokensDetails()
                .getCachedTokens());
    }

    private List<StreamChatResponse> collect(ChatRequest request) {
        List<StreamChatResponse> chunks = new ArrayList<>();
        client().streamChat(request).blockingSubscribe(chunks::add);
        return chunks;
    }

    private static String repeat(String s, int times) {
        StringBuilder sb = new StringBuilder(s.length() * times);
        for (int i = 0; i < times; i++) {
            sb.append(s);
        }
        return sb.toString();
    }


    // -------------------------------------------- 缓存写入 token (2.2.2)

    /**
     * gpt-5.6 and later bill cache writes at 1.25x the plain input rate, so
     * the write count has to arrive and has to be its own bucket.
     * <p>
     * The two calls are sent in order on purpose: the first writes the prompt
     * into the cache, the second reads it back. Both report the same
     * prompt_tokens, split differently.
     */
    @Test
    public void gpt56ReportsCacheWritesThenCacheReads() {
        String marker = "cache-write-probe-" + System.nanoTime();

        ChatRequest first = writeProbe(marker);
        ChatResponse writing = client().chat(first);
        assertNotNull(writing.getUsage().getPromptTokensDetails());
        Integer written = writing.getUsage().getPromptTokensDetails().getCacheWriteTokens();
        assertNotNull(written,
                "gpt-5.6 must report cache_write_tokens — a wrong wire name shows up here");

        ChatResponse reading = client().chat(writeProbe(marker));
        Integer cached = reading.getUsage().getPromptTokensDetails().getCachedTokens();
        assertNotNull(cached);

        assertTrue(written > 0 || cached > 0,
                "one of the two calls should have touched the cache; wrote " + written
                        + ", read " + cached + ". Caching is best-effort, so an occasional "
                        + "double miss is the vendor's call, not an SDK fault");

        // the invariant every bill rests on, checked against a live response.
        // The arithmetic lives here, not in the SDK — vendors differ and the
        // SDK reports each one's fields as that vendor defines them.
        for (ChatResponse r : new ChatResponse[]{writing, reading}) {
            int cachedPart = zeroIfNull(r.getUsage().getPromptTokensDetails()
                    .getCachedTokens());
            int writtenPart = zeroIfNull(r.getUsage().getPromptTokensDetails()
                    .getCacheWriteTokens());
            assertTrue(cachedPart + writtenPart <= r.getUsage().getPromptTokens(),
                    "cached and written are disjoint subsets of prompt_tokens; got "
                            + cachedPart + " + " + writtenPart + " vs "
                            + r.getUsage().getPromptTokens());
        }
    }

    /**
     * gpt-5.5 predates write accounting and reports no write field. Its
     * absence must stay null rather than becoming a silent 0, so a bill can
     * tell "this model has no write charge" from "wrote nothing this time".
     */
    @Test
    public void theOlderModelStillReportsNoWriteField() {
        ChatResponse response = client().chat(request());

        assertNotNull(response.getUsage().getPromptTokensDetails());
        org.junit.jupiter.api.Assertions.assertNull(
                response.getUsage().getPromptTokensDetails().getCacheWriteTokens(),
                "gpt-5.5 should report no cache_write_tokens; if it now does, the "
                        + "billing code needs the 1.25x write rate for this model too");
    }

    private ChatRequest writeProbe(String marker) {
        return ChatRequest.builder()
                .model(CACHE_MODEL)
                .messages(Arrays.asList(
                        ChatMessage.builder().role("system")
                                .content(LONG_SYSTEM + marker).build(),
                        ChatMessage.builder().role("user").content("Reply with: ok").build()))
                .build();
    }

    private static int zeroIfNull(Integer v) {
        return v == null ? 0 : v;
    }


    // ------------------------------- max_completion_tokens / 缓存参数 (2.6.0)

    /**
     * Current models refuse max_tokens, so before 2.6.0 the SDK could not cap
     * output on them at all: the only field it had was a guaranteed 400.
     */
    @Test
    public void maxTokensIsRefusedAndMaxCompletionTokensWorks() {
        ChatRequest withLegacy = ChatRequest.builder()
                .model(MODEL)
                .messages(Collections.singletonList(
                        ChatMessage.builder().role("user").content("Reply with: ok").build()))
                .maxTokens(16)
                .build();

        me.vacuity.ai.sdk.common.VacSdkException e =
                org.junit.jupiter.api.Assertions.assertThrows(
                        me.vacuity.ai.sdk.common.VacSdkException.class,
                        () -> client().chat(withLegacy));
        assertTrue(e.getMessage().contains("max_completion_tokens"),
                "the server should point at the replacement: " + e.getMessage());

        ChatResponse ok = client().chat(ChatRequest.builder()
                .model(MODEL)
                .messages(Collections.singletonList(
                        ChatMessage.builder().role("user").content("Reply with: ok").build()))
                .maxCompletionTokens(16)
                .build());
        assertNotNull(ok.getUsage(), "max_completion_tokens must be accepted");
        assertTrue(ok.getUsage().getCompletionTokens() <= 16,
                "the cap must actually bind; got " + ok.getUsage().getCompletionTokens());
    }

    /**
     * prompt_cache_key must partition the cache, not merely label it: the same
     * bytes under a second key have to miss.
     * <p>
     * Order matters — key A twice, then key B — and the marker keeps this run
     * from reading a cache entry left by an earlier one.
     */
    @Test
    public void promptCacheKeyPartitionsTheCache() {
        String marker = "cache-key-probe-" + System.nanoTime();

        Integer firstWrite = cacheWrite(cacheKeyRequest(marker, "tenant-A"));
        assertNotNull(firstWrite, "gpt-5.6 reports cache writes");

        Integer secondCached = cached(cacheKeyRequest(marker, "tenant-A"));
        assertTrue(secondCached > 0,
                "the same key must read back what it wrote; got " + secondCached);

        Integer otherKeyCached = cached(cacheKeyRequest(marker, "tenant-B"));
        assertEquals(0, otherKeyCached,
                "a different prompt_cache_key must not see tenant-A's entry; got "
                        + otherKeyCached + " cached, so the keys are not partitioning");
    }

    /**
     * The two prompt_cache_options fields this endpoint accepts. Its ttl takes
     * only "30m" here, and prewarm is rejected — which is why neither a 12h
     * option nor a prewarm field is modelled.
     */
    /**
     * prompt_cache_options is model-gated: gpt-5.5 rejects it outright with
     * {@code prompt_cache_options is not supported on this model}, so this
     * runs on CACHE_MODEL.
     */
    @Test
    public void promptCacheOptionsAcceptsModeAndThirtyMinuteTtl() {
        String marker = "cache-opts-probe-" + System.nanoTime();

        ChatResponse implicit = client().chat(cacheOptionsRequest(marker,
                me.vacuity.ai.sdk.openai.entity.PromptCacheOptions.builder()
                        .mode("implicit").ttl("30m").build()));
        assertNotNull(implicit.getUsage().getPromptTokensDetails());

        me.vacuity.ai.sdk.common.VacSdkException e =
                org.junit.jupiter.api.Assertions.assertThrows(
                        me.vacuity.ai.sdk.common.VacSdkException.class,
                        () -> client().chat(cacheOptionsRequest(marker,
                                me.vacuity.ai.sdk.openai.entity.PromptCacheOptions.builder()
                                        .ttl("12h").build())));
        assertTrue(e.getMessage().contains("30m"),
                "only a 30-minute ttl is accepted here: " + e.getMessage());
    }

    private ChatRequest cacheKeyRequest(String marker, String key) {
        return ChatRequest.builder()
                .model(CACHE_MODEL)
                .messages(Arrays.asList(
                        ChatMessage.builder().role("system")
                                .content(LONG_SYSTEM + marker).build(),
                        ChatMessage.builder().role("user").content("Reply with: ok").build()))
                .maxCompletionTokens(8)
                .promptCacheKey(key)
                .build();
    }

    private ChatRequest cacheOptionsRequest(String marker,
            me.vacuity.ai.sdk.openai.entity.PromptCacheOptions options) {
        return ChatRequest.builder()
                .model(CACHE_MODEL)
                .messages(Arrays.asList(
                        ChatMessage.builder().role("system")
                                .content(LONG_SYSTEM + marker).build(),
                        ChatMessage.builder().role("user").content("Reply with: ok").build()))
                .maxCompletionTokens(8)
                .promptCacheOptions(options)
                .build();
    }

    private Integer cacheWrite(ChatRequest request) {
        return client().chat(request).getUsage().getPromptTokensDetails().getCacheWriteTokens();
    }

    private Integer cached(ChatRequest request) {
        return client().chat(request).getUsage().getPromptTokensDetails().getCachedTokens();
    }
}
