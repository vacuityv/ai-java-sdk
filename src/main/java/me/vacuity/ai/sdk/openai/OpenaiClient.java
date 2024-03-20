package me.vacuity.ai.sdk.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import me.vacuity.ai.sdk.openai.api.OpenaiApi;
import me.vacuity.ai.sdk.openai.assistant.constant.AssistantStreamEventsConstant;
import me.vacuity.ai.sdk.openai.assistant.entity.Assistant;
import me.vacuity.ai.sdk.openai.assistant.entity.AssistantFile;
import me.vacuity.ai.sdk.openai.assistant.entity.AssistantMessage;
import me.vacuity.ai.sdk.openai.assistant.entity.AssistantMessageDelta;
import me.vacuity.ai.sdk.openai.assistant.entity.AssistantMessageFile;
import me.vacuity.ai.sdk.openai.assistant.entity.AssistantResponseBodyCallback;
import me.vacuity.ai.sdk.openai.assistant.entity.AssistantSSE;
import me.vacuity.ai.sdk.openai.assistant.entity.AssistantStreamResponse;
import me.vacuity.ai.sdk.openai.assistant.entity.Run;
import me.vacuity.ai.sdk.openai.assistant.entity.RunStep;
import me.vacuity.ai.sdk.openai.assistant.entity.RunStepDelta;
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
import me.vacuity.ai.sdk.openai.entity.ChatFunction;
import me.vacuity.ai.sdk.openai.entity.ChatFunctionCall;
import me.vacuity.ai.sdk.openai.entity.ChatFunctionCallMixIn;
import me.vacuity.ai.sdk.openai.entity.ChatFunctionMixIn;
import me.vacuity.ai.sdk.openai.entity.DeleteStatus;
import me.vacuity.ai.sdk.openai.entity.ListRequest;
import me.vacuity.ai.sdk.openai.entity.Model;
import me.vacuity.ai.sdk.openai.entity.ResponseBodyCallback;
import me.vacuity.ai.sdk.openai.entity.SSE;
import me.vacuity.ai.sdk.openai.error.ChatResponseError;
import me.vacuity.ai.sdk.openai.exception.VacSdkException;
import me.vacuity.ai.sdk.openai.file.entity.OpenaiFile;
import me.vacuity.ai.sdk.openai.interceptor.OpenaiAuthenticationInterceptor;
import me.vacuity.ai.sdk.openai.request.ChatRequest;
import me.vacuity.ai.sdk.openai.response.ChatResponse;
import me.vacuity.ai.sdk.openai.response.StreamChatResponse;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:10
 **/

public class OpenaiClient {

    private static final String BASE_URL = "https://api.openai.com";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final ObjectMapper mapper = defaultObjectMapper();


    private final OpenaiApi api;
    private final ExecutorService executorService;

    public OpenaiClient(final String token) {
        ObjectMapper mapper = defaultObjectMapper();
        OkHttpClient client = defaultClient(token, DEFAULT_TIMEOUT);
        Retrofit retrofit = defaultRetrofit(client, mapper, null);

        this.api = retrofit.create(OpenaiApi.class);
        this.executorService = client.dispatcher().executorService();
    }

    public OpenaiClient(final String token, final Duration timeout) {
        ObjectMapper mapper = defaultObjectMapper();
        OkHttpClient client = defaultClient(token, timeout);
        Retrofit retrofit = defaultRetrofit(client, mapper, null);

        this.api = retrofit.create(OpenaiApi.class);
        this.executorService = client.dispatcher().executorService();
    }

    public OpenaiClient(final String token, final Duration timeout, String baseUrl) {
        ObjectMapper mapper = defaultObjectMapper();
        OkHttpClient client = defaultClient(token, timeout);
        Retrofit retrofit = defaultRetrofit(client, mapper, baseUrl);

        this.api = retrofit.create(OpenaiApi.class);
        this.executorService = client.dispatcher().executorService();
    }

    public OpenaiClient(OpenaiApi api) {
        this.api = api;
        this.executorService = null;
    }

    public static ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.addMixIn(ChatFunction.class, ChatFunctionMixIn.class);
//        mapper.addMixIn(ChatRequest.class, ChatRequestMixIn.class);
        mapper.addMixIn(ChatFunctionCall.class, ChatFunctionCallMixIn.class);
        return mapper;
    }

    public static OkHttpClient defaultClient(String apiKey, Duration timeout) {
        return new OkHttpClient.Builder()
                .addInterceptor(new OpenaiAuthenticationInterceptor(apiKey))
                .connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }

    public static Retrofit defaultRetrofit(OkHttpClient client, ObjectMapper mapper, String baseUrl) {
        if (baseUrl == null || "".equals(baseUrl)) {
            baseUrl = BASE_URL;
        }
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public static <T> T execute(Single<T> apiCall) {
        try {
            return apiCall.blockingGet();
        } catch (HttpException e) {
            try {
                if (e.response() == null || e.response().errorBody() == null) {
                    throw e;
                }
                String errorBody = e.response().errorBody().string();
                ChatResponseError error = defaultObjectMapper().readValue(errorBody, ChatResponseError.class);
                VacSdkException ve = new VacSdkException("-1", "error", error);
                throw ve;
            } catch (IOException ex) {
                // couldn't parse error
                throw e;
            }
        }
    }


    public static <T> Flowable<T> stream(Call<ResponseBody> apiCall, Class<T> cl) {
        return stream(apiCall).map(sse -> {
            if (sse.getData() == null || "".equals(sse.getData())) {
                return null;
            } else {
                return mapper.readValue(sse.getData(), cl);
            }
        });
    }

    public static Flowable<SSE> stream(Call<ResponseBody> apiCall) {
        return stream(apiCall, false);
    }

    public static Flowable<SSE> stream(Call<ResponseBody> apiCall, boolean emitDone) {
        return Flowable.create(emitter -> apiCall.enqueue(new ResponseBodyCallback(emitter, emitDone)), BackpressureStrategy.BUFFER);
    }


    // check document here: https://platform.openai.com/docs/api-reference/assistants-streaming/events
    public Flowable<AssistantStreamResponse> assistantStream(Call<ResponseBody> apiCall) {
        Flowable<AssistantSSE> sse = Flowable.create(emitter -> apiCall.enqueue(new AssistantResponseBodyCallback(emitter)), BackpressureStrategy.BUFFER);
        return sse.map(s -> {
            try {
                AssistantStreamResponse response = new AssistantStreamResponse();
                response.setEvent(s.getEvent());
                if (s.getEvent() == null) {
                    return response;
                }
                switch (s.getEvent()) {
                    case AssistantStreamEventsConstant.THREAD_CREATED:
                        response.setDataClass(Thread.class);
                        response.setThread(mapper.readValue(s.getData(), Thread.class));
                        break;
                    case AssistantStreamEventsConstant.THREAD_RUN_CREATED:
                    case AssistantStreamEventsConstant.THREAD_RUN_QUEUED:
                    case AssistantStreamEventsConstant.THREAD_RUN_IN_PROGRESS:
                    case AssistantStreamEventsConstant.THREAD_RUN_REQUIRES_ACTION:
                    case AssistantStreamEventsConstant.THREAD_RUN_COMPLETED:
                    case AssistantStreamEventsConstant.THREAD_RUN_FAILED:
                    case AssistantStreamEventsConstant.THREAD_RUN_CANCELLING:
                    case AssistantStreamEventsConstant.THREAD_RUN_CANCELLED:
                    case AssistantStreamEventsConstant.THREAD_RUN_EXPIRED:
                        response.setDataClass(Run.class);
                        response.setRun(mapper.readValue(s.getData(), Run.class));
                        break;
                    case AssistantStreamEventsConstant.THREAD_RUN_STEP_CREATED:
                    case AssistantStreamEventsConstant.THREAD_RUN_STEP_IN_PROGRESS:
                    case AssistantStreamEventsConstant.THREAD_RUN_STEP_COMPLETED:
                    case AssistantStreamEventsConstant.THREAD_RUN_STEP_FAILED:
                    case AssistantStreamEventsConstant.THREAD_RUN_STEP_CANCELLED:
                    case AssistantStreamEventsConstant.THREAD_RUN_STEP_EXPIRED:
                        response.setDataClass(RunStep.class);
                        response.setRunStep(mapper.readValue(s.getData(), RunStep.class));
                        break;
                    case AssistantStreamEventsConstant.THREAD_RUN_STEP_DELTA:
                        response.setDataClass(RunStepDelta.class);
                        response.setRunStepDelta(mapper.readValue(s.getData(), RunStepDelta.class));
                        break;
                    case AssistantStreamEventsConstant.THREAD_MESSAGE_CREATED:
                    case AssistantStreamEventsConstant.THREAD_MESSAGE_IN_PROGRESS:
                    case AssistantStreamEventsConstant.THREAD_MESSAGE_COMPLETED:
                    case AssistantStreamEventsConstant.THREAD_MESSAGE_INCOMPLETE:
                        response.setDataClass(AssistantMessage.class);
                        response.setMessage(mapper.readValue(s.getData(), AssistantMessage.class));
                        break;
                    case AssistantStreamEventsConstant.THREAD_MESSAGE_DELTA:
                        response.setDataClass(AssistantMessageDelta.class);
                        response.setMessageDelta(mapper.readValue(s.getData(), AssistantMessageDelta.class));
                        break;
                    case AssistantStreamEventsConstant.ERROR:
                        response.setDataClass(ChatResponseError.ChatResponseErrorDetails.class);
                        response.setError(mapper.readValue(s.getData(), ChatResponseError.ChatResponseErrorDetails.class));
                        break;
                    case AssistantStreamEventsConstant.DONE:
                        break;
                    default:
                        break;
                }
                return response;
            } catch (JsonProcessingException e) {
                throw new VacSdkException("-1", "error process stream json");
            }
        });
    }


    public ChatResponse chat(ChatRequest request) throws JsonProcessingException {
        System.out.println(mapper.writeValueAsString(request));
        return execute(api.chat(request));
    }

    public Flowable<StreamChatResponse> streamChat(ChatRequest request) {
        request.setStream(true);
        return stream(api.streamChat(request), StreamChatResponse.class);
    }

    public List<Model> listModels() {
        return execute(api.listModels()).data;
    }

    public OpenaiFile uploadFile(String purpose, String filepath) {
        java.io.File file = new java.io.File(filepath);
        RequestBody purposeBody = RequestBody.create(MultipartBody.FORM, purpose);
        RequestBody fileBody = RequestBody.create(MediaType.parse("text"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", filepath, fileBody);
        return execute(api.uploadFile(purposeBody, body));
    }

    public List<OpenaiFile> listFiles(String purpose) {
        return execute(api.listFiles(purpose)).data;
    }

    public OpenaiFile retrieveFile(String fileId) {
        return execute(api.retrieveFile(fileId));
    }

    public DeleteStatus deleteFile(String fileId) {
        return execute(api.deleteFile(fileId));
    }

    public ResponseBody retrieveFileContent(String fileId) {
        return execute(api.retrieveFileContent(fileId));
    }

    public Assistant createAssistant(AssistantRequest request) {
        return execute(api.createAssistant(request));
    }

    public AssistantFile createAssistantFile(String assistantId, AssistantFileRequest fileRequest) {
        return execute(api.createAssistantFile(assistantId, fileRequest));
    }

    public List<Assistant> listAssistants(ListRequest request) {
        Map<String, Object> queryParameters = mapper.convertValue(request, new TypeReference<Map<String, Object>>() {
        });
        return execute(api.listAssistants(queryParameters)).data;
    }

    public List<AssistantFile> listAssistantFiles(String assistantId, ListRequest request) {
        Map<String, Object> queryParameters = mapper.convertValue(request, new TypeReference<Map<String, Object>>() {
        });
        return execute(api.listAssistantFiles(assistantId, queryParameters)).data;
    }

    public Assistant retrieveAssistant(String assistantId) {
        return execute(api.retrieveAssistant(assistantId));
    }

    public AssistantFile retrieveAssistantFile(String assistantId, String fileId) {
        return execute(api.retrieveAssistantFile(assistantId, fileId));
    }

    public Assistant modifyAssistant(String assistantId, ModifyAssistantRequest request) {
        return execute(api.modifyAssistant(assistantId, request));
    }

    public DeleteStatus deleteAssistant(String assistantId) {
        return execute(api.deleteAssistant(assistantId));
    }

    public DeleteStatus deleteAssistantFile(String assistantId, String fileId) {
        return execute(api.deleteAssistantFile(assistantId, fileId));
    }

    public Thread createThread(ThreadRequest request) {
        return execute(api.createThread(request));
    }

    public Thread retrieveThread(String threadId) {
        return execute(api.retrieveThread(threadId));
    }

    public Thread modifyThread(String threadId, ThreadRequest request) {
        return execute(api.modifyThread(threadId, request));
    }

    public DeleteStatus deleteThread(String threadId) {
        return execute(api.deleteThread(threadId));
    }

    public AssistantMessage createMessage(String threadId, AssistantMessageRequest request) {
        return execute(api.createMessage(threadId, request));
    }

    public List<AssistantMessage> listMessages(String threadId) {
        return execute(api.listMessages(threadId)).data;
    }

    public List<AssistantMessageFile> listMessageFiles(String threadId, String messageId, ListRequest params) {
        Map<String, Object> queryParameters = mapper.convertValue(params, new TypeReference<Map<String, Object>>() {
        });
        return execute(api.listMessageFiles(threadId, messageId, queryParameters)).data;
    }

    public AssistantMessage retrieveMessage(String threadId, String messageId) {
        return execute(api.retrieveMessage(threadId, messageId));
    }

    public AssistantMessageFile retrieveMessageFile(String threadId, String messageId, String fileId) {
        return execute(api.retrieveMessageFile(threadId, messageId, fileId));
    }

    public AssistantMessage modifyMessage(String threadId, String messageId, ModifyAssistantMessageRequest request) {
        return execute(api.modifyMessage(threadId, messageId, request));
    }

    public Run createRun(String threadId, RunRequest runRequest) {
        return execute(api.createRun(threadId, runRequest));
    }

    public Run createThreadAndRun(ThreadAndRunRequest threadAndRunRequest) {
        return execute(api.createThreadAndRun(threadAndRunRequest));
    }

    public List<Run> listRuns(String threadId, ListRequest listRequest) {
        Map<String, String> search = new HashMap<>();
        if (listRequest != null) {
            ObjectMapper mapper = defaultObjectMapper();
            search = mapper.convertValue(listRequest, Map.class);
        }
        return execute(api.listRuns(threadId, search)).data;
    }

    public List<RunStep> listRunSteps(String threadId, String runId, ListRequest listRequest) {
        Map<String, Object> search = new HashMap<>();
        if (listRequest != null) {
            ObjectMapper mapper = defaultObjectMapper();
            search = mapper.convertValue(listRequest, Map.class);
        }
        return execute(api.listRunSteps(threadId, runId, search)).data;
    }

    public Run retrieveRun(String threadId, String runId) {
        return execute(api.retrieveRun(threadId, runId));
    }

    public RunStep retrieveRunStep(String threadId, String runId, String stepId) {
        return execute(api.retrieveRunStep(threadId, runId, stepId));
    }

    public Run modifyRun(String threadId, String runId, Map<String, Object> metadata) {
        return execute(api.modifyRun(threadId, runId, metadata));
    }

    public Run submitToolOutputs(String threadId, String runId, SubmitToolOutputsRequest submitToolOutputsRequest) {
        return execute(api.submitToolOutputs(threadId, runId, submitToolOutputsRequest));
    }

    public Run cancelRun(String threadId, String runId) {
        return execute(api.cancelRun(threadId, runId));
    }


    public Flowable<AssistantStreamResponse> streamCreateRun(String threadId, RunRequest runRequest) {
        runRequest.setStream(true);
        return assistantStream(api.streamCreateRun(threadId, runRequest));
    }

    public Flowable<AssistantStreamResponse> streamCreateThreadAndRun(ThreadAndRunRequest threadAndRunRequest) {
        return assistantStream(api.streamCreateThreadAndRun(threadAndRunRequest));
    }

    public Flowable<AssistantStreamResponse> streamSubmitToolOutputs(String threadId, String runId, SubmitToolOutputsRequest submitToolOutputsRequest) {
        return assistantStream(api.streamSubmitToolOutputs(threadId, runId, submitToolOutputsRequest));
    }

}
