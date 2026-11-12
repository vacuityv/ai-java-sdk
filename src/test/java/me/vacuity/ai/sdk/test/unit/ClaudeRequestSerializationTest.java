package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import me.vacuity.ai.sdk.claude.entity.SystemMessageOutputConfig;
import me.vacuity.ai.sdk.claude.ClaudeClient;
import me.vacuity.ai.sdk.claude.entity.BlockBinding;
import me.vacuity.ai.sdk.claude.entity.CacheControl;
import me.vacuity.ai.sdk.claude.entity.ChatFunction;
import me.vacuity.ai.sdk.claude.entity.ChatMessage;
import me.vacuity.ai.sdk.claude.entity.ChatMessageContent;
import me.vacuity.ai.sdk.claude.entity.ContextManagement;
import me.vacuity.ai.sdk.claude.entity.JsonOutputFormat;
import me.vacuity.ai.sdk.claude.entity.McpServer;
import me.vacuity.ai.sdk.claude.entity.OutputConfig;
import me.vacuity.ai.sdk.claude.entity.Thinking;
import me.vacuity.ai.sdk.claude.entity.TokenTaskBudget;
import me.vacuity.ai.sdk.claude.entity.ToolChoice;
import me.vacuity.ai.sdk.claude.request.ChatRequest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Wire-format tests for the Claude ChatRequest. These assert the exact JSON
 * keys the Messages API expects — a wrong key is silently accepted by the
 * builder and only fails at request time.
 */
public class ClaudeRequestSerializationTest {

    private static final ObjectMapper MAPPER = ClaudeClient.defaultObjectMapper();

    private JsonNode serialize(ChatRequest request) throws Exception {
        return MAPPER.readTree(MAPPER.writeValueAsString(request));
    }

    private ChatRequest.ChatRequestBuilder minimal() {
        return ChatRequest.builder()
                .model("claude-opus-5")
                .messages(Collections.singletonList(new ChatMessage("user", "hi")))
                .maxTokens(1024);
    }

    @Test
    public void systemAcceptsPlainString() throws Exception {
        JsonNode json = serialize(minimal().system("you are helpful").build());
        assertTrue(json.get("system").isTextual());
        assertEquals("you are helpful", json.get("system").asText());
    }

    @Test
    public void systemAcceptsCacheableTextBlocks() throws Exception {
        ChatMessageContent block = new ChatMessageContent();
        block.setType("text");
        block.setText("large shared prompt");
        block.setCacheControl(CacheControl.ephemeral("1h"));

        JsonNode json = serialize(minimal().system(Collections.singletonList(block)).build());

        assertTrue(json.get("system").isArray());
        JsonNode first = json.get("system").get(0);
        assertEquals("text", first.get("type").asText());
        assertEquals("ephemeral", first.get("cache_control").get("type").asText());
        assertEquals("1h", first.get("cache_control").get("ttl").asText());
    }

    @Test
    public void cacheControlDefaultsToNoTtl() throws Exception {
        JsonNode json = serialize(minimal().cacheControl(CacheControl.ephemeral()).build());
        assertEquals("ephemeral", json.get("cache_control").get("type").asText());
        assertFalse(json.get("cache_control").has("ttl"));
    }

    @Test
    public void toolChoiceForcesNamedTool() throws Exception {
        JsonNode json = serialize(minimal().toolChoice(ToolChoice.tool("search")).build());
        assertEquals("tool", json.get("tool_choice").get("type").asText());
        assertEquals("search", json.get("tool_choice").get("name").asText());
    }

    @Test
    public void toolChoiceVariantsOmitUnusedName() throws Exception {
        assertEquals("auto", serialize(minimal().toolChoice(ToolChoice.auto()).build())
                .get("tool_choice").get("type").asText());
        assertEquals("any", serialize(minimal().toolChoice(ToolChoice.any()).build())
                .get("tool_choice").get("type").asText());

        JsonNode none = serialize(minimal().toolChoice(ToolChoice.none()).build());
        assertEquals("none", none.get("tool_choice").get("type").asText());
        assertFalse(none.get("tool_choice").has("name"));
    }

    @Test
    public void toolChoiceCarriesDisableParallelToolUse() throws Exception {
        ToolChoice choice = ToolChoice.any();
        choice.setDisableParallelToolUse(true);
        JsonNode json = serialize(minimal().toolChoice(choice).build());
        assertTrue(json.get("tool_choice").get("disable_parallel_tool_use").asBoolean());
    }

    @Test
    public void thinkingAdaptiveCarriesDisplay() throws Exception {
        JsonNode json = serialize(minimal().thinking(Thinking.adaptive("summarized")).build());
        assertEquals("adaptive", json.get("thinking").get("type").asText());
        assertEquals("summarized", json.get("thinking").get("display").asText());
        assertFalse(json.get("thinking").has("budget_tokens"));
    }

    @Test
    public void thinkingOmitsDisplayWhenUnset() throws Exception {
        JsonNode json = serialize(minimal().thinking(Thinking.adaptive()).build());
        assertEquals("adaptive", json.get("thinking").get("type").asText());
        assertFalse(json.get("thinking").has("display"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void legacyBudgetTokensStillSerializes() throws Exception {
        // Deprecated, but must keep working on Opus 4.6 / Sonnet 4.6.
        JsonNode json = serialize(minimal().thinking(new Thinking("enabled", 4096)).build());
        assertEquals("enabled", json.get("thinking").get("type").asText());
        assertEquals(4096, json.get("thinking").get("budget_tokens").asInt());
    }

    @Test
    public void outputConfigCarriesEffortFormatAndTaskBudget() throws Exception {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        JsonNode json = serialize(minimal().outputConfig(OutputConfig.builder()
                .effort("xhigh")
                .format(JsonOutputFormat.jsonSchema(schema))
                .taskBudget(TokenTaskBudget.tokens(64000))
                .build()).build());

        JsonNode cfg = json.get("output_config");
        assertEquals("xhigh", cfg.get("effort").asText());
        assertEquals("json_schema", cfg.get("format").get("type").asText());
        assertEquals("object", cfg.get("format").get("schema").get("type").asText());
        assertEquals("tokens", cfg.get("task_budget").get("type").asText());
        assertEquals(64000, cfg.get("task_budget").get("total").asInt());
    }

    @Test
    public void toolCarriesStrictDeferLoadingAllowedCallersAndCacheControl() throws Exception {
        ChatFunction tool = ChatFunction.builder()
                .name("search")
                .description("web search")
                .strict(true)
                .deferLoading(true)
                .allowedCallers(Collections.singletonList("code_execution_20260120"))
                .cacheControl(CacheControl.ephemeral())
                .executor(SearchArgs.class, args -> null)
                .build();

        JsonNode tools = serialize(minimal().tools(Collections.singletonList(tool)).build()).get("tools");
        JsonNode first = tools.get(0);

        assertEquals("search", first.get("name").asText());
        assertTrue(first.get("strict").asBoolean());
        assertTrue(first.get("defer_loading").asBoolean());
        assertEquals("code_execution_20260120", first.get("allowed_callers").get(0).asText());
        assertEquals("ephemeral", first.get("cache_control").get("type").asText());
        // the executor is a Java-side callback and must never reach the wire
        assertFalse(first.has("executor"));
        // input_schema is generated from the executor's argument class
        assertEquals("object", first.get("input_schema").get("type").asText());
    }

    @Test
    public void newTopLevelParametersUseSnakeCaseKeys() throws Exception {
        JsonNode json = serialize(minimal()
                .serviceTier("auto")
                .speed("fast")
                .inferenceGeo("us")
                .fallbackCreditToken("fct_abc")
                .fallbacks("default")
                .mcpServers(Collections.singletonList(McpServer.url("linear", "https://mcp.linear.app/mcp")))
                .contextManagement(ContextManagement.clearToolUses())
                .build());

        assertEquals("auto", json.get("service_tier").asText());
        assertEquals("fast", json.get("speed").asText());
        assertEquals("us", json.get("inference_geo").asText());
        assertEquals("fct_abc", json.get("fallback_credit_token").asText());
        assertEquals("default", json.get("fallbacks").asText());

        JsonNode mcp = json.get("mcp_servers").get(0);
        assertEquals("url", mcp.get("type").asText());
        assertEquals("linear", mcp.get("name").asText());

        assertEquals("clear_tool_uses_20250919",
                json.get("context_management").get("edits").get(0).get("type").asText());
    }

    @Test
    public void betasAreHeaderOnlyAndNeverEnterTheBody() throws Exception {
        JsonNode json = serialize(minimal()
                .betas(Arrays.asList("fast-mode-2026-02-01", "task-budgets-2026-03-13"))
                .build());
        assertFalse(json.has("betas"));
    }

    @Test
    public void unsetOptionalsAreOmitted() throws Exception {
        // temperature/top_p/top_k return a 400 on current models, so an unset
        // field must not be serialized as null.
        JsonNode json = serialize(minimal().build());
        assertFalse(json.has("temperature"));
        assertFalse(json.has("top_p"));
        assertFalse(json.has("top_k"));
        assertFalse(json.has("tool_choice"));
        assertFalse(json.has("thinking"));
        assertFalse(json.has("system"));
    }

    // ------------------------------------- 逐条消息 effort (2.4.0)

    /**
     * The exact wire shape, which the API is strict about:
     * {@code {"role":"system","content":[],"output_config":{"effort":"low"}}}.
     * <p>
     * The fragile part is {@code content}: it must be present and empty. A
     * mapper configured with NON_EMPTY inclusion instead of NON_NULL would
     * drop the empty list and the field would vanish — the same way Gemini's
     * googleSearch tool was silently unsendable before 1.13.0.
     */
    @Test
    public void effortOnlyMessageSerializesWithAnEmptyContentArray() throws Exception {
        ChatMessage message = ChatMessage.effortOnly("low");

        JsonNode sent = MAPPER.valueToTree(message);
        assertEquals("system", sent.get("role").asText());
        assertTrue(sent.has("content"), "content must be present, not omitted");
        assertTrue(sent.get("content").isArray(), "content must be an array");
        assertEquals(0, sent.get("content").size(), "content must be empty");
        assertEquals("low", sent.get("output_config").get("effort").asText());
        // output_config is a sibling of role/content, not nested in content
        assertTrue(sent.get("output_config").isObject());
        assertEquals(3, sent.size(), "no other field may be sent: " + sent);
    }

    /**
     * An ordinary message must not grow an output_config key.
     */
    @Test
    public void ordinaryMessagesCarryNoOutputConfig() throws Exception {
        JsonNode sent = MAPPER.valueToTree(new ChatMessage("user", "hello"));

        assertNull(sent.get("output_config"),
                "an unset output_config must not be sent, not even as null");
        assertEquals("hello", sent.get("content").asText());
    }

    /**
     * The field is usable directly, not only through the factory — Firefly was
     * subclassing ChatMessage to get it before 2.4.0.
     */
    @Test
    public void outputConfigCanBeSetOnAnyMessageDirectly() throws Exception {
        ChatMessage message = ChatMessage.builder()
                .role("system")
                .content(java.util.Collections.emptyList())
                .outputConfig(SystemMessageOutputConfig.effort("xhigh"))
                .build();

        JsonNode sent = MAPPER.valueToTree(message);
        assertEquals("xhigh", sent.get("output_config").get("effort").asText());
        assertEquals(0, sent.get("content").size());
    }

    /**
     * The whole request, to prove the message survives being nested in one and
     * that the beta flag rides along.
     */
    @Test
    public void effortOnlyMessageSurvivesInAWholeRequest() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .model("claude-opus-5-5")
                .maxTokens(16)
                .outputConfig(OutputConfig.builder().effort("high").build())
                .betas(java.util.Collections.singletonList(
                        "mid-conversation-output-config-2026-07-01"))
                .messages(java.util.Arrays.asList(
                        new ChatMessage("user", "hi"),
                        new ChatMessage("assistant", "hello"),
                        ChatMessage.effortOnly("low"),
                        new ChatMessage("user", "Reply with: ok")))
                .build();

        JsonNode sent = MAPPER.valueToTree(request);
        JsonNode effortMessage = sent.get("messages").get(2);
        assertEquals("system", effortMessage.get("role").asText());
        assertEquals(0, effortMessage.get("content").size());
        assertEquals("low", effortMessage.get("output_config").get("effort").asText());
        // the top-level effort stays as it was — the docs say to leave it alone
        assertEquals("high", sent.get("output_config").get("effort").asText());
    }

    // ------------------------------------------- clear_at (2.6.0)

    /**
     * A turn-scoped system message: renders for one turn, then stays in the
     * transcript cleared. The point is to avoid editing history — deleting a
     * reminder breaks the cache from that point and, on models with preserved
     * thinking, invalidates later thinking blocks.
     */
    @Test
    public void turnScopedSystemMessageSerializes() throws Exception {
        JsonNode sent = MAPPER.valueToTree(ChatMessage.turnScopedSystem("Keep it to one word."));

        assertEquals("system", sent.get("role").asText());
        assertEquals("Keep it to one word.", sent.get("content").asText());
        assertEquals("next_user_message", sent.get("clear_at").asText());
        assertNull(sent.get("clearAt"), "the Java name must not reach the wire");
        assertEquals(3, sent.size(), "nothing else may be sent: " + sent);
    }

    @Test
    public void clearAtCanBeSetDirectlyAndAcceptsNever() throws Exception {
        JsonNode sent = MAPPER.valueToTree(ChatMessage.builder()
                .role("system")
                .content("x")
                .clearAt("never")
                .build());

        assertEquals("never", sent.get("clear_at").asText());
    }

    @Test
    public void anOrdinaryMessageCarriesNoClearAt() throws Exception {
        JsonNode sent = MAPPER.valueToTree(new ChatMessage("user", "hello"));

        assertNull(sent.get("clear_at"),
                "an unset clear_at must not be sent, not even as null");
    }

    /**
     * The server refuses output_config on a turn-scoped message, so the two
     * 2.4.0/2.6.0 features cannot be combined. Nothing stops a caller
     * building it, but the shape is pinned here so the combination is at least
     * visible in the tests.
     */
    @Test
    public void clearAtAndOutputConfigAreASeparateConcern() throws Exception {
        ChatMessage both = ChatMessage.effortOnly("low");
        both.setClearAt("next_user_message");

        JsonNode sent = MAPPER.valueToTree(both);
        assertEquals("next_user_message", sent.get("clear_at").asText());
        assertNotNull(sent.get("output_config"));
        // the server rejects exactly this with
        // "output_config is not permitted on a turn-scoped system message"
    }

    public static class SearchArgs {
        private String query;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }

    @Test
    public void thinkingCarriesBlockBinding() throws Exception {
        Thinking thinking = Thinking.adaptive();
        thinking.setBlockBinding(BlockBinding.dropBlock());

        JsonNode json = serialize(minimal().thinking(thinking).build());

        assertEquals("drop_block",
                json.get("thinking").get("block_binding").get("prefix_mismatch_behavior").asText());
    }

    @Test
    public void blockBindingErrorFactoryMatchesTheApiDefault() throws Exception {
        Thinking thinking = Thinking.adaptive();
        thinking.setBlockBinding(BlockBinding.error());

        JsonNode json = serialize(minimal().thinking(thinking).build());

        assertEquals("error",
                json.get("thinking").get("block_binding").get("prefix_mismatch_behavior").asText());
    }

    @Test
    public void blockBindingOmittedWhenUnset() throws Exception {
        JsonNode json = serialize(minimal().thinking(Thinking.adaptive()).build());
        assertFalse(json.get("thinking").has("block_binding"));
    }

    /**
     * between_tools takes no other field — the API rejects display alongside
     * it — so the factory must emit type alone.
     */
    @Test
    public void betweenToolsSerializesTypeOnly() throws Exception {
        JsonNode json = serialize(minimal().thinking(Thinking.betweenTools()).build());

        JsonNode thinking = json.get("thinking");
        assertEquals("between_tools", thinking.get("type").asText());
        assertEquals(1, thinking.size(), "between_tools accepts no other field, got " + thinking);
    }
}
