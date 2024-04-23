package me.vacuity.ai.sdk.gemini.api;

import io.reactivex.Single;
import me.vacuity.ai.sdk.gemini.request.ChatRequest;
import me.vacuity.ai.sdk.gemini.response.ChatResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface GeminiApi {

    @POST("v1beta/models/{model}:generateContent")
    Single<ChatResponse> chat(@Path("model") String model, @Query("key") String apiKey, @Body ChatRequest request);


    @Streaming
    @POST("v1beta/models/{model}:streamGenerateContent")
    Call<ResponseBody> streamChat(@Path("model") String model, @Query("key") String apiKey, @Body ChatRequest request);
}
