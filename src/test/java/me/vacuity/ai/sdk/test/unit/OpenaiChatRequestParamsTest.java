package me.vacuity.ai.sdk.test.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.entity.ChatMessage;
import me.vacuity.ai.sdk.openai.entity.PromptCacheOptions;
import me.vacuity.ai.sdk.openai.request.ChatRequest;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The chat-completions request fields added in 2.6.0.
 *
 * @author vacuity
 */
public class OpenaiChatRequestParamsTest {

    private final ObjectMapper mapper = OpenaiClient.defaultObjectMapper();

    private ChatRequest.ChatRequestBuilder base() {
        return ChatRequest.builder()
                .model("gpt-5.6-terra")
                .messages(Collections.singletonList(
                        ChatMessage.builder().role("user").content("Reply with: ok").build()));
    }

    /**
     * Current models refuse max_tokens outright, so before 2.6.0 the SDK had
     * no way to cap output on them: the only field it offered was a 400.
     */
    @Test
    public void maxCompletionTokensSerializesUnderItsOwnWireName() throws Exception {
        JsonNode sent = mapper.valueToTree(base().maxCompletionTokens(16).build());

        assertEquals(16, sent.get("max_completion_tokens").asInt());
        assertNull(sent.get("max_tokens"), "the legacy field must not be sent alongside");
        assertNull(sent.get("maxCompletionTokens"), "the Java name must not reach the wire");
    }

    /**
     * The legacy field is still there for older models, and must stay
     * independent — sending both is rejected by the server, so the SDK must
     * not quietly emit one when the other is set.
     */
    @Test
    public void theTwoOutputCapsAreIndependent() throws Exception {
        JsonNode legacy = mapper.valueToTree(base().maxTokens(16).build());
        assertEquals(16, legacy.get("max_tokens").asInt());
        assertNull(legacy.get("max_completion_tokens"));

        JsonNode both = mapper.valueToTree(base().maxTokens(16).maxCompletionTokens(32).build());
        assertEquals(16, both.get("max_tokens").asInt());
        assertEquals(32, both.get("max_completion_tokens").asInt());
    }

    @Test
    public void promptCacheKeySerializes() throws Exception {
        JsonNode sent = mapper.valueToTree(base().promptCacheKey("tenant-A").build());

        assertEquals("tenant-A", sent.get("prompt_cache_key").asText());
    }

    @Test
    public void promptCacheOptionsSerializesModeAndTtl() throws Exception {
        JsonNode sent = mapper.valueToTree(base()
                .promptCacheOptions(PromptCacheOptions.builder()
                        .mode("implicit").ttl("30m").build())
                .build());

        JsonNode options = sent.get("prompt_cache_options");
        assertEquals("implicit", options.get("mode").asText());
        assertEquals("30m", options.get("ttl").asText());
        assertEquals(2, options.size(), "nothing else may be sent: " + options);
    }

    /**
     * prewarm is documented in OpenAI's caching guide but rejected by this
     * endpoint, so it is not modelled. If it is ever added here, this test is
     * what should change with it.
     */
    @Test
    public void promptCacheOptionsHasNoPrewarmField() throws Exception {
        JsonNode sent = mapper.valueToTree(PromptCacheOptions.mode("implicit"));

        assertNull(sent.get("prewarm"),
                "this endpoint answers Unknown parameter for prompt_cache_options.prewarm");
        assertEquals(1, sent.size());
    }

    @Test
    public void unsetNewFieldsStayOffTheWire() throws Exception {
        JsonNode sent = mapper.valueToTree(base().build());

        for (String absent : new String[]{
                "max_completion_tokens", "prompt_cache_key", "prompt_cache_options"}) {
            assertNull(sent.get(absent), "unset field must not be sent: " + absent);
        }
        assertTrue(sent.has("model"));
    }
}
