package me.vacuity.ai.sdk.test.unit;

import io.reactivex.Single;
import me.vacuity.ai.sdk.claude.ClaudeClient;
import me.vacuity.ai.sdk.claude.api.ClaudeApi;
import me.vacuity.ai.sdk.claude.entity.ChatMessage;
import me.vacuity.ai.sdk.claude.request.ChatRequest;
import me.vacuity.ai.sdk.claude.response.ChatResponse;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import retrofit2.Call;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The anthropic-beta header is what gates every beta-only parameter, so verify
 * the client actually routes to the header-carrying API method — and that
 * requests without betas keep the pre-existing behaviour.
 */
public class ClaudeBetaHeaderTest {

    /**
     * Records which overload the client called.
     */
    private static class RecordingApi implements ClaudeApi {
        String capturedBetas;
        boolean legacyChatCalled;
        boolean betaChatCalled;
        boolean legacyStreamCalled;
        boolean betaStreamCalled;

        @Override
        public Single<ChatResponse> chat(ChatRequest request) {
            legacyChatCalled = true;
            return Single.just(new ChatResponse());
        }

        @Override
        public Call<ResponseBody> streamChat(ChatRequest request) {
            legacyStreamCalled = true;
            return null;
        }

        @Override
        public Single<ChatResponse> chat(String betas, ChatRequest request) {
            betaChatCalled = true;
            capturedBetas = betas;
            return Single.just(new ChatResponse());
        }

        @Override
        public Call<ResponseBody> streamChat(String betas, ChatRequest request) {
            betaStreamCalled = true;
            capturedBetas = betas;
            return null;
        }
    }

    private ChatRequest.ChatRequestBuilder minimal() {
        return ChatRequest.builder()
                .model("claude-opus-5")
                .messages(Collections.singletonList(new ChatMessage("user", "hi")))
                .maxTokens(16);
    }

    @Test
    public void withoutBetasUsesTheLegacyOverload() {
        RecordingApi api = new RecordingApi();
        new ClaudeClient(api).chat(minimal().build());

        assertTrue(api.legacyChatCalled);
        assertNull(api.capturedBetas);
    }

    @Test
    public void emptyBetaListAlsoUsesTheLegacyOverload() {
        RecordingApi api = new RecordingApi();
        new ClaudeClient(api).chat(minimal().betas(Collections.emptyList()).build());

        assertTrue(api.legacyChatCalled);
    }

    @Test
    public void betasAreJoinedWithCommas() {
        RecordingApi api = new RecordingApi();
        new ClaudeClient(api).chat(minimal()
                .betas(Arrays.asList("fast-mode-2026-02-01", "task-budgets-2026-03-13"))
                .build());

        assertTrue(api.betaChatCalled);
        assertEquals("fast-mode-2026-02-01,task-budgets-2026-03-13", api.capturedBetas);
    }

    @Test
    public void singleBetaIsSentVerbatim() {
        RecordingApi api = new RecordingApi();
        new ClaudeClient(api).chat(minimal()
                .betas(Collections.singletonList("context-management-2025-06-27"))
                .build());

        assertEquals("context-management-2025-06-27", api.capturedBetas);
    }

    @Test
    public void streamChatRoutesOnBetasAndSetsStreamFlag() {
        RecordingApi withBetas = new RecordingApi();
        ChatRequest request = minimal()
                .betas(Collections.singletonList("fast-mode-2026-02-01"))
                .build();
        // Flowable.create is lazy, so no HTTP call is enqueued here.
        new ClaudeClient(withBetas).streamChat(request);

        assertTrue(withBetas.betaStreamCalled);
        assertEquals("fast-mode-2026-02-01", withBetas.capturedBetas);
        assertTrue(request.getStream());

        RecordingApi withoutBetas = new RecordingApi();
        new ClaudeClient(withoutBetas).streamChat(minimal().build());
        assertTrue(withoutBetas.legacyStreamCalled);
    }
}
