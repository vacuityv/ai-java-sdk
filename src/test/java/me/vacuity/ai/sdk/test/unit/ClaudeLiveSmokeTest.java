package me.vacuity.ai.sdk.test.unit;

import me.vacuity.ai.sdk.claude.ClaudeClient;
import me.vacuity.ai.sdk.claude.entity.CacheControl;
import me.vacuity.ai.sdk.claude.entity.ChatFunction;
import me.vacuity.ai.sdk.claude.entity.ChatMessage;
import me.vacuity.ai.sdk.claude.entity.ChatMessageContent;
import me.vacuity.ai.sdk.claude.entity.OutputConfig;
import me.vacuity.ai.sdk.claude.entity.Thinking;
import me.vacuity.ai.sdk.claude.entity.ToolChoice;
import me.vacuity.ai.sdk.claude.request.ChatRequest;
import me.vacuity.ai.sdk.claude.response.ChatResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke tests that call the real Messages API. Skipped unless CLAUDE_API_KEY
 * is set, so the default {@code mvn test} stays offline and free.
 * <p>
 * These verify what the offline tests structurally cannot: that Anthropic
 * actually accepts the parameters this SDK now sends. Run with:
 * <pre>
 *   CLAUDE_API_KEY=sk-ant-... mvn test -Dtest=ClaudeLiveSmokeTest
 * </pre>
 * Each case uses a small model and a tiny max_tokens to keep the cost trivial.
 */
@EnabledIfEnvironmentVariable(named = "CLAUDE_API_KEY", matches = ".+")
public class ClaudeLiveSmokeTest {

    private static final String MODEL = "claude-sonnet-5";

    private ClaudeClient client() {
        String baseUrl = System.getenv("CLAUDE_BASE_URL");
        String key = System.getenv("CLAUDE_API_KEY");
        return baseUrl == null || baseUrl.isEmpty()
                ? new ClaudeClient(key, Duration.ofSeconds(120))
                : new ClaudeClient(key, Duration.ofSeconds(120), baseUrl);
    }

    private ChatRequest.ChatRequestBuilder base() {
        return ChatRequest.builder()
                .model(MODEL)
                .messages(Collections.singletonList(
                        new ChatMessage("user", "Reply with the single word: ok")))
                .maxTokens(64);
    }

    /**
     * Baseline — proves the key works and nothing in the rewrite broke a plain call.
     */
    @Test
    public void plainChatStillWorks() {
        ChatResponse response = client().chat(base().build());

        assertNotNull(response.getId());
        assertNotNull(response.getStopReason());
        assertNotNull(response.getUsage());
        assertTrue(response.getUsage().getInputTokens() > 0);
    }

    /**
     * tool_choice is the parameter that did not exist before this change.
     */
    @Test
    public void toolChoiceForcesTheNamedTool() {
        ChatFunction weather = ChatFunction.builder()
                .name("get_weather")
                .description("Get the current weather for a city.")
                .executor(WeatherArgs.class, args -> null)
                .build();

        ChatResponse response = client().chat(ChatRequest.builder()
                .model(MODEL)
                .messages(Collections.singletonList(new ChatMessage("user", "Weather in Paris?")))
                .maxTokens(256)
                .tools(Collections.singletonList(weather))
                .toolChoice(ToolChoice.tool("get_weather"))
                .build());

        assertEquals("tool_use", response.getStopReason(),
                "forcing tool_choice should produce a tool_use stop reason");
        boolean calledTool = response.getContent().stream()
                .anyMatch(c -> "tool_use".equals(c.getType()) && "get_weather".equals(c.getName()));
        assertTrue(calledTool, "the forced tool should appear in the response content");
    }

    /**
     * Proves the system-as-blocks change actually enables prompt caching:
     * the second identical call must read from cache.
     */
    @Test
    public void cacheControlOnSystemProducesACacheHit() {
        // The cacheable prefix has a token minimum; pad well past it.
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < 400; i++) {
            prefix.append("You are a meticulous assistant that answers with extreme brevity. ");
        }

        ChatMessageContent block = new ChatMessageContent();
        block.setType("text");
        block.setText(prefix.toString());
        block.setCacheControl(CacheControl.ephemeral());

        ChatRequest request = base().system(Collections.singletonList(block)).build();

        ChatResponse first = client().chat(request);
        assertNotNull(first.getUsage());

        ChatResponse second = client().chat(base()
                .system(Collections.singletonList(block))
                .build());

        Integer cacheRead = second.getUsage().getCacheReadInputTokens();
        assertNotNull(cacheRead, "usage.cache_read_input_tokens must be present");
        assertTrue(cacheRead > 0,
                "second identical request should read the cache, got " + cacheRead
                        + " (first write=" + first.getUsage().getCacheCreationInputTokens() + ")");
    }

    /**
     * The thinking config (including the new display field) must be accepted
     * and thinking blocks must come back parsed.
     * <p>
     * Note: this deliberately does NOT assert that the summary text is
     * non-empty. Verified against both this SDK and raw curl, on both the
     * proxy and api.anthropic.com directly: {@code display: "summarized"} is
     * accepted with HTTP 200 but currently returns thinking blocks whose text
     * is empty for this model at every effort level. Whether a summary is
     * produced is a server-side decision, not something the SDK controls.
     */
    @Test
    public void thinkingConfigIsAcceptedAndBlocksParse() {
        ChatResponse response = client().chat(ChatRequest.builder()
                .model(MODEL)
                .messages(Collections.singletonList(
                        new ChatMessage("user", "What is 27 * 453? Think it through.")))
                .maxTokens(4096)
                .thinking(Thinking.adaptive("summarized"))
                .outputConfig(OutputConfig.builder().effort("low").build())
                .build());

        List<String> blockTypes = new ArrayList<>();
        for (ChatMessageContent c : response.getContent()) {
            blockTypes.add(c.getType());
        }

        assertEquals("end_turn", response.getStopReason());
        assertTrue(blockTypes.contains("thinking"),
                "a thinking block should be returned, got " + blockTypes);
        assertTrue(blockTypes.contains("text"),
                "an answer text block should be returned, got " + blockTypes);
    }

    /**
     * The anthropic-beta header must be accepted by the server, not just
     * formatted correctly on our side.
     */
    @Test
    public void betaHeaderIsAcceptedByTheServer() {
        ChatResponse response = client().chat(base()
                .betas(Arrays.asList("context-management-2025-06-27"))
                .build());

        assertNotNull(response.getId(), "a request carrying a beta flag should still succeed");
    }

    public static class WeatherArgs {
        private String city;

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }
    }


    // -------------------------------------- 缓存 usage 口径 (2.2.2)

    /**
     * Claude's cache accounting is additive, and this is the live proof.
     * <p>
     * {@code inputTokens} stays small on both calls because it is the uncached
     * remainder, not the total — the opposite of the OpenAI-compatible
     * {@code promptTokens}. Billing {@code inputTokens} as a total here
     * undercharges by the whole cached prefix.
     */
    @org.junit.jupiter.api.Test
    public void cacheUsageIsAdditiveAndSplitsWritesByLifetime() {
        String marker = "cache-probe-" + System.nanoTime();
        me.vacuity.ai.sdk.claude.entity.Usage first = client()
                .chat(cachedRequest(marker)).getUsage();
        me.vacuity.ai.sdk.claude.entity.Usage second = client()
                .chat(cachedRequest(marker)).getUsage();

        int written = nz(first.getCacheCreationInputTokens());
        int read = nz(second.getCacheReadInputTokens());
        org.junit.jupiter.api.Assertions.assertTrue(written > 0 || read > 0,
                "one of the two calls should have touched the cache; wrote " + written
                        + ", read " + read + ". Caching is best-effort, so an occasional "
                        + "double miss is the vendor's call, not an SDK fault");

        // inputTokens must NOT be the whole prompt. The addition lives here,
        // not in the SDK — Claude adds where the OpenAI side subtracts, and
        // the SDK reports each vendor's fields as that vendor defines them.
        org.junit.jupiter.api.Assertions.assertTrue(
                total(first) > first.getInputTokens(),
                "the cached prefix must sit outside inputTokens; total "
                        + total(first) + " vs inputTokens " + first.getInputTokens());

        // the same prompt must size the same both times, however the buckets moved
        org.junit.jupiter.api.Assertions.assertEquals(total(first), total(second),
                "the same prompt must total the same whether written or read");

        if (written > 0) {
            org.junit.jupiter.api.Assertions.assertNotNull(first.getCacheCreation(),
                    "a write must report its lifetime split — 5m bills 1.25x, 1h bills 2x");
            org.junit.jupiter.api.Assertions.assertEquals(written,
                    nz(first.getCacheCreation().getEphemeral5mInputTokens())
                            + nz(first.getCacheCreation().getEphemeral1hInputTokens()),
                    "the lifetimes must account for the whole write");
        }

        org.junit.jupiter.api.Assertions.assertNotNull(first.getSpeed(),
                "speed must be reported — fast mode bills about 2x");
    }

    private ChatRequest cachedRequest(String marker) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 140; i++) {
            sb.append("You are a meticulous reviewer of Java SDK code. ")
              .append("Follow these rules exactly and never deviate. ");
        }
        ChatMessageContent system = new ChatMessageContent();
        system.setType("text");
        system.setText(sb + marker);
        system.setCacheControl(CacheControl.builder().type("ephemeral").build());

        return ChatRequest.builder()
                .model("claude-opus-5-5")
                .maxTokens(16)
                .system(java.util.Collections.singletonList(system))
                .messages(java.util.Collections.singletonList(
                        ChatMessage.builder().role("user").content("Reply with: ok").build()))
                .build();
    }

    private static int total(me.vacuity.ai.sdk.claude.entity.Usage u) {
        return nz(u.getInputTokens()) + nz(u.getCacheCreationInputTokens())
                + nz(u.getCacheReadInputTokens());
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }


    // ------------------------------------- 逐条消息 effort (2.4.0)

    private static final String EFFORT_BETA = "mid-conversation-output-config-2026-07-01";

    /**
     * The documented position — between an assistant turn and the next user
     * turn — on a model that supports it.
     */
    @org.junit.jupiter.api.Test
    public void perMessageEffortIsAcceptedInTheDocumentedPosition() {
        ChatResponse response = client().chat(effortRequest("claude-opus-5-5",
                java.util.Arrays.asList(
                        new ChatMessage("user", "hi"),
                        new ChatMessage("assistant", "hello"),
                        ChatMessage.effortOnly("low"),
                        new ChatMessage("user", "Reply with: ok"))));

        org.junit.jupiter.api.Assertions.assertNotNull(response.getContent(),
                "a request with a per-message effort change must come back normally");
        org.junit.jupiter.api.Assertions.assertNotNull(response.getStopReason());
    }

    /**
     * An effort-only message renders nothing, so unlike a system message that
     * carries text it is accepted anywhere — the first entry included. This is
     * the one placement claim worth pinning live, because it is an exemption
     * rather than a rule, and an exemption is the kind of thing that gets
     * tightened.
     */
    @org.junit.jupiter.api.Test
    public void anEffortOnlyMessageIsAcceptedAsTheFirstEntry() {
        ChatResponse response = client().chat(effortRequest("claude-opus-5-5",
                java.util.Arrays.asList(
                        ChatMessage.effortOnly("low"),
                        new ChatMessage("user", "Reply with: ok"))));

        org.junit.jupiter.api.Assertions.assertNotNull(response.getStopReason(),
                "an effort-only message must still be exempt from the placement rules");
    }

    /**
     * A model outside the supported list must be the server's complaint, not a
     * silently dropped field — the SDK does not keep its own model list.
     */
    @org.junit.jupiter.api.Test
    public void anUnsupportedModelRejectsPerMessageEffort() {
        me.vacuity.ai.sdk.common.VacSdkException e =
                org.junit.jupiter.api.Assertions.assertThrows(
                        me.vacuity.ai.sdk.common.VacSdkException.class,
                        () -> client().chat(effortRequest("claude-sonnet-5",
                                java.util.Arrays.asList(
                                        new ChatMessage("user", "hi"),
                                        new ChatMessage("assistant", "ok"),
                                        ChatMessage.effortOnly("low"),
                                        new ChatMessage("user", "Reply with: ok")))));

        org.junit.jupiter.api.Assertions.assertTrue(
                e.getMessage().contains("per-turn effort"),
                "the server should name the capability: " + e.getMessage());
    }

    /**
     * Without the beta flag the field is rejected outright. Pinned so that a
     * caller who forgets it gets a recognisable failure rather than silently
     * unchanged effort.
     */
    @org.junit.jupiter.api.Test
    public void withoutTheBetaFlagOutputConfigOnAMessageIsRejected() {
        ChatRequest request = ChatRequest.builder()
                .model("claude-opus-5-5")
                .maxTokens(16)
                .messages(java.util.Arrays.asList(
                        new ChatMessage("user", "hi"),
                        new ChatMessage("assistant", "ok"),
                        ChatMessage.effortOnly("low"),
                        new ChatMessage("user", "Reply with: ok")))
                .build();

        me.vacuity.ai.sdk.common.VacSdkException e =
                org.junit.jupiter.api.Assertions.assertThrows(
                        me.vacuity.ai.sdk.common.VacSdkException.class,
                        () -> client().chat(request));
        org.junit.jupiter.api.Assertions.assertTrue(
                e.getMessage().contains("Extra inputs are not permitted"),
                "expected the missing-beta rejection, got: " + e.getMessage());
    }

    /**
     * "adaptive" is a thinking type, not an effort level. The server's error
     * carries the whole enum, which is why the SDK does not keep a copy.
     */
    @org.junit.jupiter.api.Test
    public void adaptiveIsNotAnEffortLevel() {
        me.vacuity.ai.sdk.common.VacSdkException e =
                org.junit.jupiter.api.Assertions.assertThrows(
                        me.vacuity.ai.sdk.common.VacSdkException.class,
                        () -> client().chat(effortRequest("claude-opus-5-5",
                                java.util.Arrays.asList(
                                        new ChatMessage("user", "hi"),
                                        new ChatMessage("assistant", "ok"),
                                        ChatMessage.effortOnly("adaptive"),
                                        new ChatMessage("user", "Reply with: ok")))));

        org.junit.jupiter.api.Assertions.assertTrue(e.getMessage().contains("xhigh"),
                "the server should list the accepted levels: " + e.getMessage());
    }

    private ChatRequest effortRequest(String model, java.util.List<ChatMessage> messages) {
        return ChatRequest.builder()
                .model(model)
                .maxTokens(16)
                .outputConfig(OutputConfig.builder().effort("high").build())
                .betas(java.util.Collections.singletonList(EFFORT_BETA))
                .messages(messages)
                .build();
    }
}
