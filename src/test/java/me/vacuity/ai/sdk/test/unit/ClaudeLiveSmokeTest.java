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
}
