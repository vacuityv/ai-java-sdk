package me.vacuity.ai.sdk.openai.api;

import io.reactivex.Single;
import me.vacuity.ai.sdk.openai.entity.Model;
import me.vacuity.ai.sdk.openai.request.ChatRequest;
import me.vacuity.ai.sdk.openai.response.ChatResponse;
import me.vacuity.ai.sdk.openai.response.ListResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-13 14:25
 **/

public interface OpenaiApi {

    @POST("/v1/chat/completions")
    Single<ChatResponse> chat(@Body ChatRequest request);


    @Streaming
    @POST("/v1/chat/completions")
    Call<ResponseBody> streamChat(@Body ChatRequest request);

    @GET("/v1/models")
    Single<ListResponse<Model>> listModels();
}
