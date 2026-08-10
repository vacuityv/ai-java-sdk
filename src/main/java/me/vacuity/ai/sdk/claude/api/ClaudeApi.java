package me.vacuity.ai.sdk.claude.api;

import io.reactivex.Single;
import me.vacuity.ai.sdk.claude.request.ChatRequest;
import me.vacuity.ai.sdk.claude.response.ChatResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Streaming;

public interface ClaudeApi {

    @Headers({"anthropic-version: 2023-06-01", "anthropic-beta: web-fetch-2025-09-10"})
    @POST("v1/messages")
    Single<ChatResponse> chat(@Body ChatRequest request);


    @Headers({"anthropic-version: 2023-06-01", "anthropic-beta: web-fetch-2025-09-10"})
    @Streaming
    @POST("v1/messages")
    Call<ResponseBody> streamChat(@Body ChatRequest request);

    /**
     * Same as {@link #chat(ChatRequest)} but with a caller-supplied
     * anthropic-beta header. A null value omits the header entirely.
     */
    @Headers("anthropic-version: 2023-06-01")
    @POST("v1/messages")
    Single<ChatResponse> chat(@Header("anthropic-beta") String betas, @Body ChatRequest request);

    /**
     * Same as {@link #streamChat(ChatRequest)} but with a caller-supplied
     * anthropic-beta header. A null value omits the header entirely.
     */
    @Headers("anthropic-version: 2023-06-01")
    @Streaming
    @POST("v1/messages")
    Call<ResponseBody> streamChat(@Header("anthropic-beta") String betas, @Body ChatRequest request);
}
