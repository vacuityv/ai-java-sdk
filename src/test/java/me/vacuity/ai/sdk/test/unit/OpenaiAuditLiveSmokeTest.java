package me.vacuity.ai.sdk.test.unit;

import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.assistant.entity.VectorStore;
import me.vacuity.ai.sdk.openai.assistant.request.VectorStoreRequest;
import me.vacuity.ai.sdk.openai.entity.ChatFunction;
import me.vacuity.ai.sdk.openai.entity.ChatMessage;
import me.vacuity.ai.sdk.openai.entity.ChatTool;
import me.vacuity.ai.sdk.openai.entity.StreamOptions;
import me.vacuity.ai.sdk.openai.realtime.entity.RealtimeClientSecret;
import me.vacuity.ai.sdk.openai.realtime.entity.RealtimeSessionConfig;
import me.vacuity.ai.sdk.openai.realtime.request.CreateRealtimeClientSecretRequest;
import me.vacuity.ai.sdk.openai.request.ChatRequest;
import me.vacuity.ai.sdk.openai.response.ChatResponse;
import me.vacuity.ai.sdk.openai.response.StreamChatResponse;
import me.vacuity.ai.sdk.openai.responses.entity.Response;
import me.vacuity.ai.sdk.openai.responses.request.ResponseRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The OpenAI fixes and fields from the 2.7.0 audit, through the SDK against
 * the live API.
 * <p>
 * Not covered live: chat {@code refusal} and {@code annotations} (not
 * reliably triggerable), and the deprecated Assistants / Threads / Runs /
 * Videos surface, which the configured proxy does not forward.
 *
 * @author vacuity
 */
@EnabledIfEnvironmentVariable(named = "VAC_OPENAI_KEY", matches = ".+")
public class OpenaiAuditLiveSmokeTest {

    private OpenaiClient client() {
        String base = System.getenv("VAC_OPENAI_BASE_URL");
        String key = System.getenv("VAC_OPENAI_KEY");
        return base == null || base.isEmpty()
                ? new OpenaiClient(key, Duration.ofSeconds(180))
                : new OpenaiClient(key, Duration.ofSeconds(180), base);
    }

    private static List<ChatMessage> ok() {
        return Collections.singletonList(ChatMessage.builder().role("user").content("Reply with: ok").build());
    }

    /**
     * P0: POST /v1/realtime/sessions has been removed upstream, so the old
     * call always failed. The GA replacement must mint a usable key.
     */
    @Test
    public void realtimeClientSecretMintsAKeyForAGaSession() {
        RealtimeClientSecret secret = client().createRealtimeClientSecret(
                CreateRealtimeClientSecretRequest.builder()
                        .session(RealtimeSessionConfig.builder()
                                .type("realtime").model("gpt-realtime").build())
                        .build());

        assertNotNull(secret.getValue(), "an ephemeral key must be returned");
        assertTrue(secret.getExpiresAt() > 0);
        assertNotNull(secret.getSession(), "the resolved session must bind");
        assertEquals("realtime", secret.getSession().getType());
        assertNotNull(secret.getSession().getAudio(),
                "the GA session carries its audio settings nested under audio");
    }

    /** P0: this was null on every response before 2.7.0. */
    @Test
    public void vectorStoreUsageBytesBinds() {
        VectorStore store = client().createVectorStore(VectorStoreRequest.builder()
                .name("sdk-audit-probe").description("created by an SDK live test").build());
        try {
            assertNotNull(store.getUsageBytes(), "usage_bytes must bind (it used to read a "
                    + "'bytes' key the API does not send)");
            assertEquals("created by an SDK live test", store.getDescription());
        } finally {
            client().deleteVectorStore(store.getId());
        }
    }

    /** P1: logprobs could be requested but never read before 2.7.0. */
    @Test
    public void chatLogprobsComeBack() {
        ChatResponse response = client().chat(ChatRequest.builder()
                .model("gpt-4.1-mini")   // gpt-5.x rejects logprobs outright
                .messages(ok()).logprobs(true).topLogprobs(2).maxCompletionTokens(16)
                .build());

        assertNotNull(response.getChoices().get(0).getLogprobs(), "choice.logprobs must bind");
        assertTrue(!response.getChoices().get(0).getLogprobs().getContent().isEmpty());
        assertNotNull(response.getChoices().get(0).getLogprobs().getContent().get(0).getTopLogprobs());
        assertNotNull(response.getServiceTier(), "service_tier must bind");
    }

    /** The new request fields must all be accepted together. */
    @Test
    public void newChatRequestFieldsAreAccepted() {
        ChatFunction noop = ChatFunction.builder()
                .name("get_weather").description("Weather for a city")
                .executor(StrictArgs.class, args -> "unused")
                .strict(true)
                .build();

        ChatResponse response = client().chat(ChatRequest.builder()
                .model("gpt-5.5")
                .messages(ok()).maxCompletionTokens(16)
                .store(true)                        // metadata is only allowed with store
                .metadata(Collections.singletonMap("src", "sdk-audit"))
                .safetyIdentifier("sdk-audit")
                .parallelToolCalls(false)
                .serviceTier("auto")
                .tools(Collections.singletonList(ChatTool.builder().type("function").function(noop).build()))
                .build());

        assertNotNull(response.getChoices());
    }

    /** obfuscation is on by default and off when include_obfuscation is false. */
    @Test
    public void streamObfuscationFollowsIncludeObfuscation() {
        assertTrue(countObfuscated(StreamOptions.builder().includeUsage(true).build()) > 0,
                "chunks carry obfuscation padding by default");
        assertEquals(0, countObfuscated(StreamOptions.builder()
                        .includeUsage(true).includeObfuscation(false).build()),
                "include_obfuscation=false must reach the server and strip the padding");
    }

    /** P1: the Responses API's cache writes, missed when chat got them in 2.3.0. */
    @Test
    public void responsesReportCacheWritesThenReads() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            sb.append("You are a meticulous Java SDK reviewer. Follow the rules. ");
        }
        String instructions = sb + Long.toString(System.nanoTime());

        Response first = client().createResponse(ResponseRequest.builder()
                .model("gpt-5.6-terra").instructions(instructions).input("Reply with: ok")
                .maxOutputTokens(16).build());
        Response second = client().createResponse(ResponseRequest.builder()
                .model("gpt-5.6-terra").instructions(instructions).input("Reply with: ok")
                .maxOutputTokens(16).build());

        Integer written = first.getUsage().getInputTokensDetails().getCacheWriteTokens();
        Integer read = second.getUsage().getInputTokensDetails().getCachedTokens();
        assertNotNull(written, "cache_write_tokens must bind on the Responses API");
        assertTrue(written > 0 || read > 0,
                "one of the two calls should touch the cache; wrote " + written + ", read " + read);
        assertNotNull(first.getToolUsage(), "tool_usage must bind");
    }

    private int countObfuscated(StreamOptions options) {
        List<StreamChatResponse> chunks = new ArrayList<>();
        client().streamChat(ChatRequest.builder()
                .model("gpt-5.5").messages(ok()).maxCompletionTokens(16)
                .streamOptions(options).build()).blockingSubscribe(chunks::add);
        int n = 0;
        for (StreamChatResponse chunk : chunks) {
            if (chunk.getObfuscation() != null) {
                n++;
            }
        }
        return n;
    }

    /**
     * Strict mode needs every property listed as required, and the generated
     * schema lists one only when it is annotated so. Without the annotation the
     * server rejects the schema.
     */
    public static class StrictArgs {
        @com.fasterxml.jackson.annotation.JsonProperty(required = true)
        public String city;
    }

    /**
     * The reverse case, pinned so the javadoc's claim stays true: without the
     * annotation, strict mode is refused for want of a required list.
     */
    @Test
    public void strictWithoutRequiredFieldsIsRefused() {
        ChatFunction loose = ChatFunction.builder()
                .name("get_weather").description("Weather for a city")
                .executor(LooseArgs.class, args -> "unused")
                .strict(true)
                .build();
        me.vacuity.ai.sdk.common.VacSdkException e =
                org.junit.jupiter.api.Assertions.assertThrows(
                        me.vacuity.ai.sdk.common.VacSdkException.class,
                        () -> client().chat(ChatRequest.builder()
                                .model("gpt-5.5").messages(ok()).maxCompletionTokens(16)
                                .tools(Collections.singletonList(ChatTool.builder()
                                        .type("function").function(loose).build()))
                                .build()));
        assertTrue(e.getMessage().contains("required"), "got: " + e.getMessage());
    }

    public static class LooseArgs {
        public String city;
    }
}
