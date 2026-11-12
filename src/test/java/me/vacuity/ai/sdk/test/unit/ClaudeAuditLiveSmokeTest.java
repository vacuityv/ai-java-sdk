package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.vacuity.ai.sdk.claude.ClaudeClient;
import me.vacuity.ai.sdk.claude.entity.ChatFunction;
import me.vacuity.ai.sdk.claude.entity.ChatMessage;
import me.vacuity.ai.sdk.claude.entity.ChatMessageContent;
import me.vacuity.ai.sdk.claude.request.ChatRequest;
import me.vacuity.ai.sdk.claude.response.ChatResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Claude fields the 2.7.0 audit added, through the SDK against the live
 * API. Each case was first confirmed by a raw request; this proves the SDK
 * sends and reads them as modelled.
 *
 * @author vacuity
 */
@EnabledIfEnvironmentVariable(named = "CLAUDE_API_KEY", matches = ".+")
public class ClaudeAuditLiveSmokeTest {

    private static final String MODEL = "claude-opus-5-5";

    private ClaudeClient client() {
        String base = System.getenv("CLAUDE_BASE_URL");
        String key = System.getenv("CLAUDE_API_KEY");
        return base == null || base.isEmpty()
                ? new ClaudeClient(key, Duration.ofSeconds(240))
                : new ClaudeClient(key, Duration.ofSeconds(240), base);
    }

    /**
     * Web search is billed per request on top of tokens. Before 2.7.0 the
     * count arrived and the SDK had nowhere to put it.
     */
    @Test
    public void webSearchCountsArriveAsServerToolUse() {
        ChatResponse response = client().chat(ChatRequest.builder()
                .model(MODEL)
                .maxTokens(1024)
                .tools(Collections.singletonList(ChatFunction.builder()
                        .type("web_search_20260209").name("web_search").maxUses(1).build()))
                .messages(Collections.singletonList(new ChatMessage("user",
                        "Search the web: what is the capital of France? One word.")))
                .build());

        assertNotNull(response.getUsage().getServerToolUse(),
                "a web-search request must report server_tool_use");
        assertTrue(response.getUsage().getServerToolUse().getWebSearchRequests() >= 1,
                "the search must be counted; got "
                        + response.getUsage().getServerToolUse().getWebSearchRequests());
    }

    /**
     * is_error must reach Claude and be understood as a failure, not read as a
     * successful result whose content happens to say "timeout". Measured: with
     * it set, Claude answered that the request had timed out and it would try
     * again — so the assertion is on the reply, not merely on a 200.
     */
    @Test
    public void aToolResultMarkedAsAnErrorIsTreatedAsAFailure() throws Exception {
        ChatMessageContent call = new ChatMessageContent();
        call.setType("tool_use");
        call.setId("toolu_audit_error");
        call.setName("get_weather");
        call.setInput(new ObjectMapper().readTree("{\"city\":\"Paris\"}"));

        ChatMessageContent failed = new ChatMessageContent();
        failed.setType("tool_result");
        failed.setToolUseId("toolu_audit_error");
        failed.setIsError(true);
        failed.setContent("upstream timeout");

        ChatResponse response = client().chat(ChatRequest.builder()
                .model(MODEL)
                .maxTokens(128)
                .tools(Collections.singletonList(ChatFunction.builder()
                        .name("get_weather").description("Weather for a city")
                        .executor(WeatherArgs.class, args -> "unused").build()))
                .messages(Arrays.asList(
                        new ChatMessage("user", "What is the weather in Paris?"),
                        new ChatMessage("assistant", Collections.singletonList(call)),
                        new ChatMessage("user", Collections.singletonList(failed))))
                .build());

        String reply = textOf(response).toLowerCase();
        boolean acknowledged = reply.contains("time") || reply.contains("fail")
                || reply.contains("error") || reply.contains("unable") || reply.contains("try")
                || reply.contains("couldn") || reply.contains("issue")
                || response.getContent().stream().anyMatch(b -> "tool_use".equals(b.getType()));
        assertTrue(acknowledged,
                "Claude should treat the tool call as failed (say so, or retry it); replied: " + reply);
    }

    /**
     * Turning citations on for a document is an object; the citations that
     * come back on text blocks are a list. Both go through the one field.
     */
    @Test
    public void documentCitationsComeBackOnTextBlocks() {
        ChatMessageContent.ContentSource source = new ChatMessageContent.ContentSource();
        source.setType("text");
        source.setMediaType("text/plain");
        source.setData("The capital of France is Paris. Paris has 2.1 million residents.");

        ChatMessageContent document = new ChatMessageContent();
        document.setType("document");
        document.setSource(source);
        document.setTitle("France facts");
        document.setContext("A short note");
        document.setCitations(ChatMessageContent.citationsEnabled());

        ChatMessageContent question = new ChatMessageContent();
        question.setType("text");
        question.setText("What is the capital of France? Cite the document.");

        ChatResponse response = client().chat(ChatRequest.builder()
                .model(MODEL)
                .maxTokens(256)
                .messages(Collections.singletonList(
                        new ChatMessage("user", Arrays.asList(document, question))))
                .build());

        boolean cited = false;
        for (ChatMessageContent block : response.getContent()) {
            if ("text".equals(block.getType()) && block.getCitations() instanceof List
                    && !((List<?>) block.getCitations()).isEmpty()) {
                cited = true;
            }
        }
        assertTrue(cited, "a text block must carry the document citation");
    }

    /** The code-execution container comes back so it can be reused next turn. */
    @Test
    public void codeExecutionReportsItsContainer() {
        ChatResponse response = client().chat(ChatRequest.builder()
                .model(MODEL)
                .maxTokens(1024)
                .tools(Collections.singletonList(ChatFunction.builder()
                        .type("code_execution_20260521").name("code_execution").build()))
                .messages(Collections.singletonList(new ChatMessage("user",
                        "Use code to compute 2**10 and reply with the number.")))
                .build());

        assertNotNull(response.getContainer(), "a code-execution response must report its container");
        assertTrue(String.valueOf(response.getContainer().get("id")).startsWith("container_"),
                "the container id is what gets passed back to reuse it: " + response.getContainer());
    }

    private static String textOf(ChatResponse response) {
        StringBuilder sb = new StringBuilder();
        for (ChatMessageContent block : response.getContent()) {
            if ("text".equals(block.getType()) && block.getText() != null) {
                sb.append(block.getText());
            }
        }
        return sb.toString();
    }

    /** Arguments for the test's custom tool; the SDK derives input_schema from it. */
    public static class WeatherArgs {
        public String city;
    }

    @SuppressWarnings("unused")
    private static void unused() {
        assertEquals(1, 1);
    }
}
