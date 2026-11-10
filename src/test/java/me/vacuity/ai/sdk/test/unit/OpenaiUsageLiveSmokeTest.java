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
        Integer cached = second.getUsage().getCachedPromptTokens();
        assertNotNull(cached, "the second call must still report a cached-token count");

        assertTrue(cached > 0,
                "the second identical call should hit the cache; got " + cached
                        + " cached of " + second.getUsage().getPromptTokens()
                        + " prompt tokens. Caching is best-effort, so an occasional 0 is "
                        + "the vendor's call, not an SDK fault");

        // the arithmetic the billing code depends on
        assertTrue(cached <= second.getUsage().getPromptTokens(),
                "cached tokens are a subset of prompt_tokens, never more");
        assertEquals(second.getUsage().getPromptTokens(),
                cached + second.getUsage().getUncachedPromptTokens(),
                "cached + uncached must equal prompt_tokens");
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
        assertNotNull(carrying.get(0).getUsage().getCachedPromptTokens());
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
}
