package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.reactivex.Flowable;
import me.vacuity.ai.sdk.claude.ClaudeClient;
import me.vacuity.ai.sdk.claude.entity.CacheControl;
import me.vacuity.ai.sdk.claude.entity.ChatMessage;
import me.vacuity.ai.sdk.claude.entity.ChatMessageContent;
import me.vacuity.ai.sdk.claude.entity.Thinking;
import me.vacuity.ai.sdk.claude.entity.ToolChoice;
import me.vacuity.ai.sdk.claude.request.ChatRequest;
import me.vacuity.ai.sdk.claude.response.ChatResponse;
import me.vacuity.ai.sdk.claude.response.StreamChatResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives ClaudeClient over a real HTTP connection to a local server, so the
 * whole OkHttp + Retrofit + Jackson path is exercised: the headers that
 * actually reach the wire, the serialized body, and response/SSE parsing.
 * <p>
 * The unit tests elsewhere stub the API interface out entirely, which means
 * they cannot catch a Retrofit-level mistake. Nothing here talks to Anthropic;
 * the server is bound to 127.0.0.1 on an ephemeral port.
 */
public class ClaudeWireIntegrationTest {

    private static final ObjectMapper MAPPER = ClaudeClient.defaultObjectMapper();

    private HttpServer server;
    private String baseUrl;

    /**
     * What the server actually received.
     */
    private final AtomicReference<String> capturedBody = new AtomicReference<>();
    private final AtomicReference<String> capturedBeta = new AtomicReference<>();
    private final AtomicReference<String> capturedApiKey = new AtomicReference<>();
    private final AtomicReference<String> capturedVersion = new AtomicReference<>();
    private final AtomicReference<String> capturedPath = new AtomicReference<>();
    private final AtomicReference<Integer> betaHeaderCount = new AtomicReference<>(0);

    /**
     * What the server should reply with.
     */
    private volatile String responseBody = "{}";
    private volatile String responseContentType = "application/json";
    private volatile int responseStatus = 200;

    @BeforeEach
    public void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handle);
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    public void stopServer() {
        server.stop(0);
    }

    private void handle(HttpExchange exchange) throws java.io.IOException {
        capturedPath.set(exchange.getRequestURI().getPath());
        capturedApiKey.set(exchange.getRequestHeaders().getFirst("x-api-key"));
        capturedVersion.set(exchange.getRequestHeaders().getFirst("anthropic-version"));

        List<String> betas = exchange.getRequestHeaders().get("anthropic-beta");
        betaHeaderCount.set(betas == null ? 0 : betas.size());
        capturedBeta.set(betas == null || betas.isEmpty() ? null : betas.get(0));

        try (InputStream in = exchange.getRequestBody()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int read;
            while ((read = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            capturedBody.set(new String(buffer.toByteArray(), StandardCharsets.UTF_8));
        }

        byte[] out = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", responseContentType);
        exchange.sendResponseHeaders(responseStatus, out.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(out);
        }
    }

    private ClaudeClient client() {
        return new ClaudeClient("test-key-not-a-real-secret", Duration.ofSeconds(10), baseUrl);
    }

    private ChatRequest.ChatRequestBuilder minimal() {
        return ChatRequest.builder()
                .model("claude-opus-5")
                .messages(Collections.singletonList(new ChatMessage("user", "hi")))
                .maxTokens(64);
    }

    private JsonNode capturedJson() throws Exception {
        return MAPPER.readTree(capturedBody.get());
    }

    // ---------------------------------------------------------------- headers

    @Test
    public void requestHitsMessagesEndpointWithAuthHeaders() throws Exception {
        responseBody = "{\"id\":\"msg_1\",\"stop_reason\":\"end_turn\"}";
        client().chat(minimal().build());

        assertEquals("/v1/messages", capturedPath.get());
        assertEquals("test-key-not-a-real-secret", capturedApiKey.get());
        assertEquals("2023-06-01", capturedVersion.get());
    }

    @Test
    public void withoutBetasTheLegacyHeaderIsStillSent() throws Exception {
        // Pre-existing behaviour must not change for callers that never set betas.
        responseBody = "{\"id\":\"msg_1\"}";
        client().chat(minimal().build());

        assertEquals("web-fetch-2025-09-10", capturedBeta.get());
        assertEquals(1, betaHeaderCount.get());
    }

    @Test
    public void betasReachTheWireAsOneCommaSeparatedHeader() throws Exception {
        responseBody = "{\"id\":\"msg_1\"}";
        client().chat(minimal()
                .betas(Arrays.asList("fast-mode-2026-02-01", "task-budgets-2026-03-13"))
                .build());

        assertEquals("fast-mode-2026-02-01,task-budgets-2026-03-13", capturedBeta.get());
        // exactly one header, not one per beta, and no leftover legacy value
        assertEquals(1, betaHeaderCount.get());
    }

    // ------------------------------------------------------------------- body

    @Test
    public void toolChoiceAndCacheableSystemReachTheWire() throws Exception {
        responseBody = "{\"id\":\"msg_1\"}";

        ChatMessageContent block = new ChatMessageContent();
        block.setType("text");
        block.setText("shared prefix");
        block.setCacheControl(CacheControl.ephemeral("1h"));

        client().chat(minimal()
                .system(Collections.singletonList(block))
                .toolChoice(ToolChoice.tool("search"))
                .thinking(Thinking.adaptive("summarized"))
                .build());

        JsonNode body = capturedJson();
        assertEquals("search", body.get("tool_choice").get("name").asText());
        assertEquals("1h", body.get("system").get(0).get("cache_control").get("ttl").asText());
        assertEquals("summarized", body.get("thinking").get("display").asText());
        // betas are a header concern and must not leak into the body
        assertTrue(body.get("betas") == null);
    }

    @Test
    public void streamFlagIsSetOnStreamingRequests() throws Exception {
        responseBody = "data: {\"type\":\"message_stop\"}\n\n";
        responseContentType = "text/event-stream";

        client().streamChat(minimal().build()).blockingSubscribe(e -> { }, t -> { });

        assertTrue(capturedJson().get("stream").asBoolean());
    }

    // --------------------------------------------------------------- response

    @Test
    public void refusalAndCacheUsageParseOffTheWire() throws Exception {
        responseBody = "{\"id\":\"msg_1\",\"type\":\"message\",\"role\":\"assistant\","
                + "\"model\":\"claude-opus-5\",\"content\":[],\"stop_reason\":\"refusal\","
                + "\"stop_details\":{\"type\":\"refusal\",\"category\":\"cyber\"},"
                + "\"usage\":{\"input_tokens\":5,\"output_tokens\":0,"
                + "\"cache_creation_input_tokens\":512,\"cache_read_input_tokens\":1536}}";

        ChatResponse response = client().chat(minimal().build());

        assertEquals("refusal", response.getStopReason());
        assertEquals("cyber", response.getStopDetails().getCategory());
        assertEquals(512, response.getUsage().getCacheCreationInputTokens());
        assertEquals(1536, response.getUsage().getCacheReadInputTokens());
    }

    @Test
    public void normalResponseLeavesStopDetailsNull() throws Exception {
        responseBody = "{\"id\":\"msg_1\",\"stop_reason\":\"end_turn\","
                + "\"usage\":{\"input_tokens\":5,\"output_tokens\":7}}";

        ChatResponse response = client().chat(minimal().build());

        assertEquals("end_turn", response.getStopReason());
        assertNull(response.getStopDetails());
        assertNull(response.getUsage().getCacheReadInputTokens());
    }

    @Test
    public void sseStreamStillParsesIntoEvents() throws Exception {
        responseContentType = "text/event-stream";
        responseBody = ""
                + "event: message_start\n"
                + "data: {\"type\":\"message_start\"}\n"
                + "\n"
                + "event: content_block_delta\n"
                + "data: {\"type\":\"content_block_delta\",\"delta\":{\"type\":\"text_delta\",\"text\":\"hello\"}}\n"
                + "\n"
                + "event: message_stop\n"
                + "data: {\"type\":\"message_stop\"}\n"
                + "\n";

        List<StreamChatResponse> events = new ArrayList<>();
        Flowable<StreamChatResponse> flow = client().streamChat(minimal()
                .betas(Collections.singletonList("fast-mode-2026-02-01"))
                .build());
        flow.blockingSubscribe(events::add);

        assertEquals(3, events.size());
        assertEquals("message_start", events.get(0).getType());
        assertEquals("content_block_delta", events.get(1).getType());
        assertEquals("hello", events.get(1).getDelta().getText());
        assertEquals("message_stop", events.get(2).getType());
        // the beta header must survive the streaming path too
        assertEquals("fast-mode-2026-02-01", capturedBeta.get());
    }

    @Test
    public void errorBodyIsSurfacedAsVacSdkException() {
        responseStatus = 400;
        responseBody = "{\"type\":\"error\",\"error\":{\"type\":\"invalid_request_error\","
                + "\"message\":\"temperature: unsupported parameter\"}}";

        Throwable thrown = null;
        try {
            client().chat(minimal().build());
        } catch (Throwable t) {
            thrown = t;
        }

        assertNotNull(thrown, "a 400 must not be swallowed");
        assertTrue(thrown.getMessage() != null
                        && thrown.getMessage().contains("unsupported parameter"),
                "error message should carry the server text, got: " + thrown.getMessage());
    }
}
