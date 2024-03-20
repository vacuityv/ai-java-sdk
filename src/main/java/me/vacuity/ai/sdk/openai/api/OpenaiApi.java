package me.vacuity.ai.sdk.openai.api;

import io.reactivex.Single;
import me.vacuity.ai.sdk.openai.assistant.entity.Assistant;
import me.vacuity.ai.sdk.openai.assistant.entity.AssistantFile;
import me.vacuity.ai.sdk.openai.assistant.entity.AssistantMessage;
import me.vacuity.ai.sdk.openai.assistant.entity.AssistantMessageFile;
import me.vacuity.ai.sdk.openai.assistant.entity.Run;
import me.vacuity.ai.sdk.openai.assistant.entity.RunStep;
import me.vacuity.ai.sdk.openai.assistant.entity.Thread;
import me.vacuity.ai.sdk.openai.assistant.request.AssistantFileRequest;
import me.vacuity.ai.sdk.openai.assistant.request.AssistantMessageRequest;
import me.vacuity.ai.sdk.openai.assistant.request.AssistantRequest;
import me.vacuity.ai.sdk.openai.assistant.request.ModifyAssistantMessageRequest;
import me.vacuity.ai.sdk.openai.assistant.request.ModifyAssistantRequest;
import me.vacuity.ai.sdk.openai.assistant.request.RunRequest;
import me.vacuity.ai.sdk.openai.assistant.request.SubmitToolOutputsRequest;
import me.vacuity.ai.sdk.openai.assistant.request.ThreadAndRunRequest;
import me.vacuity.ai.sdk.openai.assistant.request.ThreadRequest;
import me.vacuity.ai.sdk.openai.entity.DeleteStatus;
import me.vacuity.ai.sdk.openai.entity.Model;
import me.vacuity.ai.sdk.openai.file.entity.OpenaiFile;
import me.vacuity.ai.sdk.openai.request.ChatRequest;
import me.vacuity.ai.sdk.openai.response.ChatResponse;
import me.vacuity.ai.sdk.openai.response.ListResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;

import java.util.Map;

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


    @Multipart
    @POST("/v1/files")
    Single<OpenaiFile> uploadFile(@Part("purpose") RequestBody purpose, @Part MultipartBody.Part file);

    @GET("/v1/files")
    Single<ListResponse<OpenaiFile>> listFiles(@Query("purpose") String purpose);

    @GET("/v1/files/{file_id}")
    Single<OpenaiFile> retrieveFile(@Path("file_id") String fileId);

    @DELETE("/v1/files/{file_id}")
    Single<DeleteStatus> deleteFile(@Path("file_id") String fileId);

    @Streaming
    @GET("/v1/files/{file_id}/content")
    Single<ResponseBody> retrieveFileContent(@Path("file_id") String fileId);


    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("/v1/assistants")
    Single<Assistant> createAssistant(@Body AssistantRequest request);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("/v1/assistants/{assistant_id}/files")
    Single<AssistantFile> createAssistantFile(@Path("assistant_id") String assistantId, @Body AssistantFileRequest fileRequest);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("/v1/assistants")
    Single<ListResponse<Assistant>> listAssistants(@QueryMap Map<String, Object> filterRequest);


    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("/v1/assistants/{assistant_id}/files")
    Single<ListResponse<AssistantFile>> listAssistantFiles(@Path("assistant_id") String assistantId, @QueryMap Map<String, Object> filterRequest);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("/v1/assistants/{assistant_id}")
    Single<Assistant> retrieveAssistant(@Path("assistant_id") String assistantId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("/v1/assistants/{assistant_id}/files/{file_id}")
    Single<AssistantFile> retrieveAssistantFile(@Path("assistant_id") String assistantId, @Path("file_id") String fileId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("/v1/assistants/{assistant_id}")
    Single<Assistant> modifyAssistant(@Path("assistant_id") String assistantId, @Body ModifyAssistantRequest request);


    @Headers({"OpenAI-Beta: assistants=v1"})
    @DELETE("/v1/assistants/{assistant_id}")
    Single<DeleteStatus> deleteAssistant(@Path("assistant_id") String assistantId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @DELETE("/v1/assistants/{assistant_id}/files/{file_id}")
    Single<DeleteStatus> deleteAssistantFile(@Path("assistant_id") String assistantId, @Path("file_id") String fileId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("/v1/threads")
    Single<Thread> createThread(@Body ThreadRequest request);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("/v1/threads/{thread_id}")
    Single<Thread> retrieveThread(@Path("thread_id") String threadId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("/v1/threads/{thread_id}")
    Single<Thread> modifyThread(@Path("thread_id") String threadId, @Body ThreadRequest request);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @DELETE("/v1/threads/{thread_id}")
    Single<DeleteStatus> deleteThread(@Path("thread_id") String threadId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("/v1/threads/{thread_id}/messages")
    Single<AssistantMessage> createMessage(@Path("thread_id") String threadId, @Body AssistantMessageRequest request);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("/v1/threads/{thread_id}/messages")
    Single<ListResponse<AssistantMessage>> listMessages(@Path("thread_id") String threadId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("/v1/threads/{thread_id}/messages/{message_id}/files")
    Single<ListResponse<AssistantMessageFile>> listMessageFiles(@Path("thread_id") String threadId, @Path("message_id") String messageId, @QueryMap Map<String, Object> listRequest);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("/v1/threads/{thread_id}/messages/{message_id}")
    Single<AssistantMessage> retrieveMessage(@Path("thread_id") String threadId, @Path("message_id") String messageId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("/v1/threads/{thread_id}/messages/{message_id}/files/{file_id}")
    Single<AssistantMessageFile> retrieveMessageFile(@Path("thread_id") String threadId, @Path("message_id") String messageId, @Path("file_id") String fileId);


    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("/v1/threads/{thread_id}/messages/{message_id}")
    Single<AssistantMessage> modifyMessage(@Path("thread_id") String threadId, @Path("message_id") String messageId, @Body ModifyAssistantMessageRequest request);

    @Headers("OpenAI-Beta: assistants=v1")
    @POST("/v1/threads/{thread_id}/runs")
    Single<Run> createRun(@Path("thread_id") String threadId, @Body RunRequest runRequest);

    @Headers("OpenAI-Beta: assistants=v1")
    @POST("/v1/threads/runs")
    Single<Run> createThreadAndRun(@Body ThreadAndRunRequest threadAndRunRequest);

    @Headers("OpenAI-Beta: assistants=v1")
    @GET("/v1/threads/{thread_id}/runs")
    Single<ListResponse<Run>> listRuns(@Path("thread_id") String threadId, @QueryMap Map<String, String> listRequest);

    @Headers("OpenAI-Beta: assistants=v1")
    @GET("/v1/threads/{thread_id}/runs/{run_id}/steps")
    Single<ListResponse<RunStep>> listRunSteps(@Path("thread_id") String threadId, @Path("run_id") String runId, @QueryMap Map<String, Object> listRequest);

    @Headers("OpenAI-Beta: assistants=v1")
    @GET("/v1/threads/{thread_id}/runs/{run_id}")
    Single<Run> retrieveRun(@Path("thread_id") String threadId, @Path("run_id") String runId);

    @Headers("OpenAI-Beta: assistants=v1")
    @GET("/v1/threads/{thread_id}/runs/{run_id}/steps/{step_id}")
    Single<RunStep> retrieveRunStep(@Path("thread_id") String threadId, @Path("run_id") String runId, @Path("step_id") String stepId);

    @Headers("OpenAI-Beta: assistants=v1")
    @POST("/v1/threads/{thread_id}/runs/{run_id}")
    Single<Run> modifyRun(@Path("thread_id") String threadId, @Path("run_id") String runId, @Body Map<String, Object> metadata);

    @Headers("OpenAI-Beta: assistants=v1")
    @POST("/v1/threads/{thread_id}/runs/{run_id}/submit_tool_outputs")
    Single<Run> submitToolOutputs(@Path("thread_id") String threadId, @Path("run_id") String runId, @Body SubmitToolOutputsRequest submitToolOutputsRequest);

    @Headers("OpenAI-Beta: assistants=v1")
    @POST("/v1/threads/{thread_id}/runs/{run_id}/cancel")
    Single<Run> cancelRun(@Path("thread_id") String threadId, @Path("run_id") String runId);


    @Streaming
    @Headers("OpenAI-Beta: assistants=v1")
    @POST("/v1/threads/{thread_id}/runs")
    Call<ResponseBody> streamCreateRun(@Path("thread_id") String threadId, @Body RunRequest runRequest);

    @Streaming
    @Headers("OpenAI-Beta: assistants=v1")
    @POST("/v1/threads/runs")
    Call<ResponseBody> streamCreateThreadAndRun(@Body ThreadAndRunRequest threadAndRunRequest);

    @Streaming
    @Headers("OpenAI-Beta: assistants=v1")
    @POST("/v1/threads/{thread_id}/runs/{run_id}/submit_tool_outputs")
    Call<ResponseBody> streamSubmitToolOutputs(@Path("thread_id") String threadId, @Path("run_id") String runId, @Body SubmitToolOutputsRequest submitToolOutputsRequest);

}

