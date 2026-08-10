package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.vacuity.ai.sdk.claude.ClaudeClient;
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

    public static class SearchArgs {
        private String query;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }
}
