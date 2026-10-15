package me.vacuity.ai.sdk.test.unit;

import io.reactivex.Flowable;
import me.vacuity.ai.sdk.gemini.GeminiClient;
import me.vacuity.ai.sdk.gemini.entity.ChatFunction;
import me.vacuity.ai.sdk.gemini.entity.ChatMessage;
import me.vacuity.ai.sdk.gemini.entity.ChatMessageContentPart;
import me.vacuity.ai.sdk.gemini.entity.Tool;
import me.vacuity.ai.sdk.gemini.entity.ToolConfig;
import me.vacuity.ai.sdk.gemini.request.ChatRequest;
import me.vacuity.ai.sdk.gemini.response.ChatResponse;
import me.vacuity.ai.sdk.gemini.response.ChatResponseCandidate;
import me.vacuity.ai.sdk.gemini.response.StreamChatResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Smoke tests against gemini-4-argon with function declarations and the
 * googleSearch built-in tool in the same request — the combination that fails
 * without toolConfig.includeServerSideToolInvocations.
 * <p>
 * Skipped unless GEMINI_API_KEY is set.
 */
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
public class GeminiArgonLiveSmokeTest {

    private static final String MODEL = "gemini-4-argon";

    private GeminiClient client() {
        return new GeminiClient(System.getenv("GEMINI_API_KEY"), Duration.ofSeconds(180));
    }

    /**
     * Function declarations and googleSearch in one tool entry.
     */
    private List<Tool> toolsWithSearch() {
        ChatFunction weather = ChatFunction.builder()
                .name("get_weather")
                .description("Get the current weather for a city")
                .executor(WeatherArgs.class, a -> null)
                .build();
        return Collections.singletonList(
                Tool.functionsWithGoogleSearch(Collections.singletonList(weather)));
    }

    private ChatRequest.ChatRequestBuilder ask(String text) {
        return ChatRequest.builder()
                .model(MODEL)
                .contents(Collections.singletonList(ChatMessage.builder()
                        .role("user")
                        .parts(Collections.singletonList(
                                ChatMessageContentPart.builder().text(text).build()))
                        .build()))
                .tools(toolsWithSearch());
    }

    /**
     * Without the flag the server rejects the combination. This pins the reason
     * the field exists, so the test fails loudly if the requirement ever goes
     * away and the flag becomes dead weight.
     */
    @Test
    public void withoutTheFlagTheCombinationIsRejected() {
        try {
            client().chat(ask("What is the USD to JPY rate right now?").build());
            fail("expected gemini-4-argon to reject functions + googleSearch without toolConfig");
        } catch (Throwable t) {
            String msg = String.valueOf(t.getMessage());
            assertTrue(msg.contains("include_server_side_tool_invocations"),
                    "expected the server's own explanation, got: " + msg);
        }
    }

    /**
     * With the flag the same request succeeds and the model really searches.
     */
    @Test
    public void withTheFlagSearchAndFunctionsCoexist() {
        ChatResponse response = client().chat(
                ask("What is the USD to JPY exchange rate right now? Search to confirm.")
                        .toolConfig(ToolConfig.includeServerSideToolInvocations())
                        .build());

        assertNotNull(response.getCandidates());
        assertFalse(response.getCandidates().isEmpty());

        ChatResponseCandidate candidate = response.getCandidates().get(0);
        assertNotNull(candidate.getGroundingMetadata(),
                "a search-backed answer should carry groundingMetadata");

        List<ChatMessageContentPart> parts = candidate.getContent().getParts();
        assertFalse(parts.isEmpty());

        boolean sawServerTool = parts.stream()
                .anyMatch(p -> p.getToolCall() != null || p.getToolResponse() != null);
        assertTrue(sawServerTool,
                "includeServerSideToolInvocations should surface toolCall / toolResponse parts, got "
                        + describe(parts));

        // every part that carries a signature must still carry its content
        for (ChatMessageContentPart p : parts) {
            if (p.getThoughtSignature() != null) {
                boolean hasContent = p.getText() != null || p.getToolCall() != null
                        || p.getToolResponse() != null || p.getFunctionCall() != null
                        || !p.getExtra().isEmpty();
                assertTrue(hasContent,
                        "a signed part lost its content — multi-turn replay would break: " + describe(parts));
            }
        }
    }

    /**
     * Function calling still works alongside the built-in tool.
     */
    @Test
    public void functionCallingStillTriggers() {
        ChatResponse response = client().chat(
                ask("What is the weather in Beijing today? Use the tool.")
                        .toolConfig(ToolConfig.includeServerSideToolInvocations())
                        .build());

        boolean calledFunction = response.getCandidates().get(0).getContent().getParts().stream()
                .anyMatch(p -> p.getFunctionCall() != null);
        assertTrue(calledFunction,
                "expected a functionCall part, got "
                        + describe(response.getCandidates().get(0).getContent().getParts()));
    }

    /**
     * The same combination over streamGenerateContent.
     */
    @Test
    public void streamingCarriesTheFlagToo() {
        List<StreamChatResponse> events = new ArrayList<>();

        Flowable<StreamChatResponse> stream = client().streamChat(
                ask("What is the USD to JPY exchange rate right now? Search to confirm.")
                        .toolConfig(ToolConfig.includeServerSideToolInvocations())
                        .build());
        stream.blockingSubscribe(e -> {
            if (e != null) {
                events.add(e);
            }
        });

        assertFalse(events.isEmpty(), "streaming produced no events");

        boolean sawText = events.stream()
                .filter(e -> e.getCandidates() != null)
                .flatMap(e -> e.getCandidates().stream())
                .filter(c -> c.getContent() != null && c.getContent().getParts() != null)
                .flatMap(c -> c.getContent().getParts().stream())
                .anyMatch(p -> p.getText() != null && !p.getText().isEmpty());
        assertTrue(sawText, "expected at least one text chunk across " + events.size() + " events");
    }

    private static String describe(List<ChatMessageContentPart> parts) {
        List<String> shapes = new ArrayList<>();
        for (ChatMessageContentPart p : parts) {
            List<String> keys = new ArrayList<>();
            if (p.getText() != null) keys.add("text");
            if (p.getFunctionCall() != null) keys.add("functionCall");
            if (p.getToolCall() != null) keys.add("toolCall");
            if (p.getToolResponse() != null) keys.add("toolResponse");
            if (p.getThoughtSignature() != null) keys.add("thoughtSignature");
            if (!p.getExtra().isEmpty()) keys.add("extra" + p.getExtra().keySet());
            shapes.add(keys.toString());
        }
        return Arrays.toString(shapes.toArray());
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
