package me.vacuity.ai.sdk.claude.api;

import io.reactivex.Single;
import me.vacuity.ai.sdk.claude.request.ChatRequest;
import me.vacuity.ai.sdk.claude.response.ChatResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Streaming;

public interface ClaudeApi {

    @POST("/v1/messages")
    Single<ChatResponse> chat(@Body ChatRequest request);


    @Streaming
    @POST("/v1/messages")
    Call<ResponseBody> streamChat(@Body ChatRequest request);
}
