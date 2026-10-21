package me.vacuity.ai.sdk.test.unit;

import me.vacuity.ai.sdk.gemini.GeminiClient;
import me.vacuity.ai.sdk.gemini.entity.ChatMessage;
import me.vacuity.ai.sdk.gemini.entity.ChatMessageContentPart;
import me.vacuity.ai.sdk.gemini.entity.Usage;
import me.vacuity.ai.sdk.gemini.request.ChatRequest;
import me.vacuity.ai.sdk.gemini.response.ChatResponse;
import me.vacuity.ai.sdk.gemini.response.StreamChatResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies against the live API that thoughtsTokenCount really arrives, and
 * where in a stream it shows up. Skipped unless GEMINI_API_KEY is set.
 */
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
public class GeminiUsageLiveSmokeTest {

    private static final String PUZZLE =
            "一个农夫要带狼、羊、白菜过河，船每次只能载农夫和一样东西。请推理出完整步骤。";

    private GeminiClient client() {
        return new GeminiClient(System.getenv("GEMINI_API_KEY"), Duration.ofSeconds(300));
    }

    /**
     * gemini-4-argon does not think unless thinkingLevel is set — includeThoughts
     * alone yields no thinking and no thoughtsTokenCount. gemini-3.8-flash thinks
     * by default. Verified against the live API on 2026-10-21.
     */
    private ChatRequest thinkingRequest(String model) {
        ChatRequest.ThinkingConfig.ThinkingConfigBuilder thinking =
                ChatRequest.ThinkingConfig.builder().includeThoughts(Boolean.TRUE);
        if (model.startsWith("gemini-4")) {
            thinking.thinkingLevel("high");
        }
        return buildRequest(model, thinking.build());
    }

    private ChatRequest buildRequest(String model, ChatRequest.ThinkingConfig thinkingConfig) {
        return ChatRequest.builder()
                .model(model)
                .contents(Collections.singletonList(ChatMessage.builder()
                        .role("user")
                        .parts(Collections.singletonList(
                                ChatMessageContentPart.builder().text(PUZZLE).build()))
                        .build()))
                .generationConfig(ChatRequest.GenerationConfig.builder()
                        .thinkingConfig(thinkingConfig)
                        .build())
                .build();
    }

    /**
     * Pins the behaviour difference that makes the helper above necessary: on
     * gemini-4-argon, includeThoughts without thinkingLevel produces no
     * thinking at all, so no thoughtsTokenCount is reported.
     */
    @Test
    public void argonDoesNotThinkWithoutAThinkingLevel() {
        ChatResponse response = client().chat(buildRequest("gemini-4-argon",
                ChatRequest.ThinkingConfig.builder().includeThoughts(Boolean.TRUE).build()));

        assertNull(response.getUsageMetadata().getThoughtsTokenCount(),
                "argon reported thinking without a thinkingLevel — the behaviour changed, "
                        + "re-check the billing guidance in the release notes");
    }

    private void assertStreamReportsThinking(String model) {
        List<Usage> usages = new ArrayList<>();
        client().streamChat(thinkingRequest(model)).blockingSubscribe(chunk -> {
            if (chunk != null && chunk.getUsageMetadata() != null) {
                usages.add(chunk.getUsageMetadata());
            }
        });

        assertTrue(!usages.isEmpty(), model + ": no chunk carried usageMetadata");

        int firstWithThoughts = -1;
        for (int i = 0; i < usages.size(); i++) {
            if (usages.get(i).getThoughtsTokenCount() != null
                    && usages.get(i).getThoughtsTokenCount() > 0) {
                firstWithThoughts = i;
                break;
            }
        }
        assertTrue(firstWithThoughts >= 0,
                model + ": thoughtsTokenCount never appeared across "
                        + usages.size() + " chunks carrying usage");

        Usage last = usages.get(usages.size() - 1);
        assertNotNull(last.getThoughtsTokenCount(),
                model + ": the final chunk must still carry thoughtsTokenCount");
        assertTrue(last.getThoughtsTokenCount() > 0);
        assertNotNull(last.getBillableOutputTokenCount());
        assertTrue(last.getBillableOutputTokenCount() > last.getCandidatesTokenCount(),
                model + ": billable output must exceed the visible answer when the model thought");

        // cumulative, not per-chunk deltas
        Integer firstValue = usages.get(firstWithThoughts).getThoughtsTokenCount();
        assertTrue(last.getThoughtsTokenCount() >= firstValue,
                model + ": thoughtsTokenCount should be cumulative, saw "
                        + firstValue + " then " + last.getThoughtsTokenCount());

        System.out.println("[" + model + "] chunks with usage=" + usages.size()
                + ", thoughtsTokenCount first seen at usage-chunk #" + firstWithThoughts
                + ", final thoughts=" + last.getThoughtsTokenCount()
                + ", final candidates=" + last.getCandidatesTokenCount()
                + ", billable output=" + last.getBillableOutputTokenCount());
    }

    @Test
    public void argonStreamReportsThinkingTokens() {
        assertStreamReportsThinking("gemini-4-argon");
    }

    @Test
    public void flashStreamReportsThinkingTokens() {
        assertStreamReportsThinking("gemini-3.8-flash");
    }

    @Test
    public void nonStreamingAlsoReportsThinkingTokens() {
        ChatResponse response = client().chat(thinkingRequest("gemini-4-argon"));

        Usage usage = response.getUsageMetadata();
        assertNotNull(usage);
        assertNotNull(usage.getThoughtsTokenCount(),
                "non-streaming usageMetadata must carry thoughtsTokenCount");
        assertTrue(usage.getThoughtsTokenCount() > 0);
        assertTrue(usage.getBillableOutputTokenCount() > usage.getCandidatesTokenCount());

        System.out.println("[non-streaming] thoughts=" + usage.getThoughtsTokenCount()
                + ", candidates=" + usage.getCandidatesTokenCount()
                + ", billable output=" + usage.getBillableOutputTokenCount());
    }
}
