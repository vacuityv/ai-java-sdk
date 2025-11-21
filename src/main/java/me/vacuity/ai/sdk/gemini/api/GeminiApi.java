package me.vacuity.ai.sdk.gemini.api;

import io.reactivex.Single;
import me.vacuity.ai.sdk.gemini.request.ChatRequest;
import me.vacuity.ai.sdk.gemini.response.ChatResponse;
import me.vacuity.ai.sdk.gemini.video.request.VeoVideoRequest;
import me.vacuity.ai.sdk.gemini.video.response.VeoVideoResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface GeminiApi {

    @POST("v1alpha/models/{model}:generateContent")
    Single<ChatResponse> chat(@Path("model") String model, @Body ChatRequest request);


    @Streaming
    @POST("v1alpha/models/{model}:streamGenerateContent")
    Call<ResponseBody> streamChat(@Path("model") String model, @Body ChatRequest request);

    /**
     * Generate video using Veo model
     */
    @POST("v1beta/models/{model}:predictLongRunning")
    Single<VeoVideoResponse> generateVideo(@Path("model") String model, @Body VeoVideoRequest request);

    /**
     * Get video generation job status
     * Note: operationName should be the full path like "models/veo-2.0-generate-001/operations/xxx"
     */
    @GET("v1beta/{operationName}")
    Single<VeoVideoResponse> fetchVideoOperation(@Path(value = "operationName", encoded = true) String operationName);

    /**
     * Download video from Veo generation
     *
     * @param videoUrl The full video URL from VeoVideo.getUri()
     * @return ResponseBody containing the video file
     */
    @Streaming
    @GET
    Single<ResponseBody> downloadVeoVideo(@Url String videoUrl);
}
