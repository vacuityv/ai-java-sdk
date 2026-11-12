package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every field added to the claude side in 2.7.0, bound in both directions.
 * <p>
 * Generated from a diff of each class against the previous release, so the
 * set is exactly what changed — not a hand-picked sample. Each test feeds a
 * JSON object carrying the new wire names through the client's own mapper and
 * back out, and asserts every key survives with its value intact.
 * <p>
 * Matching the official spec's field names is necessary but not enough: the
 * {@code VideoJob} timestamps had names that looked right in the source and
 * still never bound, because an explicit {@code @JsonProperty} overrode the
 * client's naming strategy. A round trip through the real mapper is what
 * catches that.
 *
 * @author vacuity
 */
public class ClaudeFieldBindingTest {

    private static final ObjectMapper MAPPER = me.vacuity.ai.sdk.claude.ClaudeClient.defaultObjectMapper();

    private static void assertRoundTrip(Class<?> type, String fixture, String... wireNames)
            throws Exception {
        JsonNode in = MAPPER.readTree(fixture);
        Object bound = MAPPER.treeToValue(in, type);
        JsonNode out = MAPPER.valueToTree(bound);
        for (String wire : wireNames) {
            assertTrue(out.has(wire), type.getSimpleName() + " dropped \"" + wire
                    + "\" on the way through — it does not bind. Out: " + out);
            JsonNode a = in.get(wire), b = out.get(wire);
            // A Long field writes back as a LongNode where the input parsed as an
            // IntNode; the value is what matters, not Jackson's node subclass.
            boolean same = a.isNumber() && b.isNumber()
                    ? a.decimalValue().compareTo(b.decimalValue()) == 0
                    : a.equals(b);
            assertTrue(same, type.getSimpleName() + "." + wire
                    + " changed in the round trip: " + a + " -> " + b);
        }
    }

    /** 2 field(s) added in 2.7.0 on entity.ChatFunction. */
    @Test
    public void roundTrip_ChatFunction() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.claude.entity.ChatFunction.class,
                "{\"eager_input_streaming\":true,\"input_examples\":[{\"k\":\"v\"}]}",
                "eager_input_streaming", "input_examples");
    }
    /** 9 field(s) added in 2.7.0 on entity.ChatMessageContent. */
    @Test
    public void roundTrip_ChatMessageContent() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.claude.entity.ChatMessageContent.class,
                "{\"caller\":{\"k\":\"v\"},\"citations\":{\"k\":\"v\"},\"container\":{\"k\":\"v\"},\"context\":\"s_context\",\"is_error\":true,\"stop_details\":{},\"title\":\"s_title\",\"toolset_name\":\"s_toolset_name\",\"transformations\":{\"k\":\"v\"}}",
                "caller", "citations", "container", "context", "is_error", "stop_details", "title", "toolset_name", "transformations");
    }
    /** 2 field(s) added in 2.7.0 on entity.ServerToolUsage. */
    @Test
    public void roundTrip_ServerToolUsage() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.claude.entity.ServerToolUsage.class,
                "{\"web_fetch_requests\":7,\"web_search_requests\":7}",
                "web_fetch_requests", "web_search_requests");
    }
    /** 3 field(s) added in 2.7.0 on entity.Usage. */
    @Test
    public void roundTrip_Usage() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.claude.entity.Usage.class,
                "{\"fallback_credit\":{\"k\":\"v\"},\"iterations\":{\"k\":\"v\"},\"server_tool_use\":{}}",
                "fallback_credit", "iterations", "server_tool_use");
    }
    /** 4 field(s) added in 2.7.0 on request.ChatRequest. */
    @Test
    public void roundTrip_ChatRequest() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.claude.request.ChatRequest.class,
                "{\"compaction\":{\"k\":\"v\"},\"thread\":{\"k\":\"v\"},\"user_profile_id\":\"s_user_profile_id\",\"workspace_id\":\"s_workspace_id\"}",
                "compaction", "thread", "user_profile_id", "workspace_id");
    }
    /** 3 field(s) added in 2.7.0 on response.ChatResponse. */
    @Test
    public void roundTrip_ChatResponse() throws Exception {
        assertRoundTrip(me.vacuity.ai.sdk.claude.response.ChatResponse.class,
                "{\"container\":{\"k\":\"v\"},\"context_management\":{\"k\":\"v\"},\"diagnostics\":{\"k\":\"v\"}}",
                "container", "context_management", "diagnostics");
    }
}
