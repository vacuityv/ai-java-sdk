package me.vacuity.ai.sdk.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import me.vacuity.ai.sdk.common.VacSdkException;
import me.vacuity.ai.sdk.openai.api.OpenaiApi;
import me.vacuity.ai.sdk.openai.assistant.constant.AssistantStreamEventsConstant;
import me.vacuity.ai.sdk.openai.assistant.entity.Assistant;
import me.vacuity.ai.sdk.openai.assistant.entity.AssistantMessage;
import me.vacuity.ai.sdk.openai.assistant.entity.AssistantMessageDelta;
import me.vacuity.ai.sdk.openai.assistant.entity.AssistantResponseBodyCallback;
import me.vacuity.ai.sdk.openai.assistant.entity.AssistantSSE;
import me.vacuity.ai.sdk.openai.assistant.entity.AssistantStreamResponse;
import me.vacuity.ai.sdk.openai.assistant.entity.Run;
import me.vacuity.ai.sdk.openai.assistant.entity.RunStep;
import me.vacuity.ai.sdk.openai.assistant.entity.RunStepDelta;
import me.vacuity.ai.sdk.openai.assistant.entity.Thread;
import me.vacuity.ai.sdk.openai.assistant.entity.VectorStore;
import me.vacuity.ai.sdk.openai.assistant.entity.VectorStoreFile;
import me.vacuity.ai.sdk.openai.assistant.entity.VectorStoreFileBatch;
import me.vacuity.ai.sdk.openai.assistant.request.AssistantMessageRequest;
import me.vacuity.ai.sdk.openai.assistant.request.AssistantRequest;
import me.vacuity.ai.sdk.openai.assistant.request.ModifyAssistantMessageRequest;
import me.vacuity.ai.sdk.openai.assistant.request.ModifyAssistantRequest;
import me.vacuity.ai.sdk.openai.assistant.request.ModifyVectorStoreRequest;
import me.vacuity.ai.sdk.openai.assistant.request.RunRequest;
import me.vacuity.ai.sdk.openai.assistant.request.SubmitToolOutputsRequest;
import me.vacuity.ai.sdk.openai.assistant.request.ThreadAndRunRequest;
import me.vacuity.ai.sdk.openai.assistant.request.ThreadRequest;
import me.vacuity.ai.sdk.openai.assistant.request.VectorStoreFileBatchRequest;
import me.vacuity.ai.sdk.openai.assistant.request.VectorStoreFileRequest;
import me.vacuity.ai.sdk.openai.assistant.request.VectorStoreRequest;
import me.vacuity.ai.sdk.openai.batch.entity.Batch;
import me.vacuity.ai.sdk.openai.batch.request.CreateBatchRequest;
import me.vacuity.ai.sdk.openai.entity.ChatFunction;
import me.vacuity.ai.sdk.openai.entity.ChatFunctionCall;
import me.vacuity.ai.sdk.openai.entity.ChatFunctionCallMixIn;
import me.vacuity.ai.sdk.openai.entity.ChatFunctionMixIn;
import me.vacuity.ai.sdk.openai.entity.DeleteStatus;
import me.vacuity.ai.sdk.openai.entity.ListRequest;
import me.vacuity.ai.sdk.openai.entity.Model;
import me.vacuity.ai.sdk.openai.error.ChatResponseError;
import me.vacuity.ai.sdk.openai.file.entity.OpenaiFile;
import me.vacuity.ai.sdk.openai.image.request.CreateImageRequest;
import me.vacuity.ai.sdk.openai.image.request.EditImageRequest;
import me.vacuity.ai.sdk.openai.image.request.ImageVariationRequest;
import me.vacuity.ai.sdk.openai.image.response.ImageResponse;
import me.vacuity.ai.sdk.openai.image.response.ImageStreamEvent;
import me.vacuity.ai.sdk.openai.interceptor.OpenaiAuthenticationInterceptor;
import me.vacuity.ai.sdk.openai.video.entity.VideoJob;
import me.vacuity.ai.sdk.openai.video.request.CreateVideoRequest;
import me.vacuity.ai.sdk.openai.video.request.RemixVideoRequest;
import me.vacuity.ai.sdk.openai.realtime.entity.RealtimeSession;
import me.vacuity.ai.sdk.openai.realtime.request.CreateRealtimeSessionRequest;
import me.vacuity.ai.sdk.openai.request.ChatRequest;
import me.vacuity.ai.sdk.openai.response.ChatResponse;
import me.vacuity.ai.sdk.openai.responses.entity.Response;
import me.vacuity.ai.sdk.openai.responses.entity.ResponseStreamEvent;
import me.vacuity.ai.sdk.openai.responses.request.ResponseRequest;
import me.vacuity.ai.sdk.openai.response.StreamChatResponse;
import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.net.Proxy;
import java.time.Duration;
import java.util.ArrayList;
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
    private final OkHttpClient httpClient;
    private final ExecutorService executorService;

    public OpenaiClient(final String token) {
        OkHttpClient client = defaultClient(token, DEFAULT_TIMEOUT);
        Retrofit retrofit = defaultRetrofit(client, mapper, null);

        this.api = retrofit.create(OpenaiApi.class);
        this.httpClient = client;
        this.executorService = client.dispatcher().executorService();
    }

    public OpenaiClient(final String token, final Duration timeout) {
        OkHttpClient client = defaultClient(token, timeout);
        Retrofit retrofit = defaultRetrofit(client, mapper, null);

        this.api = retrofit.create(OpenaiApi.class);
        this.httpClient = client;
        this.executorService = client.dispatcher().executorService();
    }

    public OpenaiClient(final String token, final Duration timeout, String baseUrl) {
        OkHttpClient client = defaultClient(token, timeout);
        Retrofit retrofit = defaultRetrofit(client, mapper, baseUrl);

        this.api = retrofit.create(OpenaiApi.class);
        this.httpClient = client;
        this.executorService = client.dispatcher().executorService();
    }

    public OpenaiClient(OpenaiApi api) {
        this.api = api;
        this.httpClient = null;
        this.executorService = null;
    }

    public OpenaiClient(final String token, final Duration timeout, Proxy proxy) {
        OkHttpClient client = defaultClient(token, timeout)
                .newBuilder()
                .proxy(proxy)
                .build();
        Retrofit retrofit = defaultRetrofit(client, mapper, null);
        this.api = retrofit.create(OpenaiApi.class);
        this.httpClient = client;
        this.executorService = client.dispatcher().executorService();
    }

    public OpenaiClient(final String token, final Duration timeout, Proxy proxy, String proxyUsername, String proxyPassword) {
        Authenticator proxyAuthenticator = (route, response) -> {
            String credential = Credentials.basic(proxyUsername, proxyPassword);
            return response.request().newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build();
        };
        OkHttpClient client = defaultClient(token, timeout)
                .newBuilder()
                .proxy(proxy)
                .proxyAuthenticator(proxyAuthenticator)
                .build();
        Retrofit retrofit = defaultRetrofit(client, mapper, null);
        this.api = retrofit.create(OpenaiApi.class);
        this.httpClient = client;
        this.executorService = client.dispatcher().executorService();
    }

    public OpenaiClient(final String token, final Duration timeout, Proxy proxy, Authenticator proxyAuthenticator) {
        OkHttpClient client = defaultClient(token, timeout)
                .newBuilder()
                .proxy(proxy)
                .proxyAuthenticator(proxyAuthenticator)
                .build();
        Retrofit retrofit = defaultRetrofit(client, mapper, null);
        this.api = retrofit.create(OpenaiApi.class);
        this.httpClient = client;
        this.executorService = client.dispatcher().executorService();
    }

    public static ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.addMixIn(ChatFunction.class, ChatFunctionMixIn.class);
        mapper.addMixIn(ChatFunctionCall.class, ChatFunctionCallMixIn.class);
        return mapper;
    }

    public static OkHttpClient defaultClient(String apiKey, Duration timeout) {
        return new OkHttpClient.Builder()
                .addInterceptor(new OpenaiAuthenticationInterceptor(apiKey))
                .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
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
                ChatResponseError error = mapper.readValue(errorBody, ChatResponseError.class);
                VacSdkException ve = new VacSdkException(error.getError().getCode(), error.getError().getMessage(), error);
                throw ve;
            } catch (IOException ex) {
                // couldn't parse error
                throw e;
            }
        }
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
                throw new VacSdkException("-1", "error process stream json", null);
            }
        });
    }


    public ChatResponse chat(ChatRequest request) {
        request.setStream(false);
        return execute(api.chat(request));
    }

    public Flowable<StreamChatResponse> streamChat(ChatRequest request) {
        request.setStream(true);
        return eventSourceStream(api.streamChat(request), StreamChatResponse.class);
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

    public List<Assistant> listAssistants(ListRequest request) {
        Map<String, Object> queryParameters = mapper.convertValue(request, new TypeReference<Map<String, Object>>() {
        });
        return execute(api.listAssistants(queryParameters)).data;
    }


    public Assistant retrieveAssistant(String assistantId) {
        return execute(api.retrieveAssistant(assistantId));
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

    public List<AssistantMessage> listMessages(String threadId, ListRequest params) {
        Map<String, Object> queryParameters = mapper.convertValue(params, new TypeReference<Map<String, Object>>() {
        });
        return execute(api.listMessages(threadId, queryParameters)).data;
    }

    public AssistantMessage retrieveMessage(String threadId, String messageId) {
        return execute(api.retrieveMessage(threadId, messageId));
    }

    public AssistantMessage modifyMessage(String threadId, String messageId, ModifyAssistantMessageRequest request) {
        return execute(api.modifyMessage(threadId, messageId, request));
    }

    public Run createRun(String threadId, RunRequest runRequest) {
        runRequest.setStream(false);
        return execute(api.createRun(threadId, runRequest));
    }

    public Run createThreadAndRun(ThreadAndRunRequest threadAndRunRequest) {
        threadAndRunRequest.setStream(false);
        return execute(api.createThreadAndRun(threadAndRunRequest));
    }

    public List<Run> listRuns(String threadId, ListRequest listRequest) {
        Map<String, String> search = new HashMap<>(8);
        if (listRequest != null) {
            search = mapper.convertValue(listRequest, Map.class);
        }
        return execute(api.listRuns(threadId, search)).data;
    }

    public List<RunStep> listRunSteps(String threadId, String runId, ListRequest listRequest) {
        Map<String, Object> search = new HashMap<>(8);
        if (listRequest != null) {
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
        threadAndRunRequest.setStream(true);
        return assistantStream(api.streamCreateThreadAndRun(threadAndRunRequest));
    }

    public Flowable<AssistantStreamResponse> streamSubmitToolOutputs(String threadId, String runId, SubmitToolOutputsRequest submitToolOutputsRequest) {
        submitToolOutputsRequest.setStream(true);
        return assistantStream(api.streamSubmitToolOutputs(threadId, runId, submitToolOutputsRequest));
    }

    public ImageResponse createImage(CreateImageRequest request) {
        return execute(api.createImage(request));
    }

    public ImageResponse editImage(EditImageRequest request, String imagePath, String maskPath) {
        java.io.File image = new java.io.File(imagePath);
        java.io.File mask = null;
        if (maskPath != null) {
            mask = new java.io.File(maskPath);
        }
        return editImage(request, image, mask);
    }

    public ImageResponse editImage(EditImageRequest request, List<String> imagePaths, String maskPath) {
        List<java.io.File> images = new ArrayList<>(imagePaths.size());
        for (String imagePath : imagePaths) {
            java.io.File image = new java.io.File(imagePath);
            images.add(image);
        }
        java.io.File maskFile = null;
        if (maskPath != null) {
            maskFile = new java.io.File(maskPath);
        }

        return editImage(request, maskFile, images);
    }

    public ImageResponse editImage(EditImageRequest request, java.io.File image, java.io.File mask) {
        List<java.io.File> images = new ArrayList<>(1);
        images.add(image);
        return editImage(request, mask, images);
    }

    private String getMediaTypeStr(java.io.File file) {
        String fileName = file.getName();
        String mediaTypeStr;
        if (fileName.endsWith(".png")) {
            mediaTypeStr = "image/png";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            mediaTypeStr = "image/jpeg";
        } else if (fileName.endsWith(".gif")) {
            mediaTypeStr = "image/gif";
        } else if (fileName.endsWith(".webp")) {
            mediaTypeStr = "image/webp";
        } else {
            mediaTypeStr = "application/octet-stream"; // 默认二进制
        }
        return mediaTypeStr;

    }

    public ImageResponse editImage(EditImageRequest request, java.io.File mask, List<java.io.File> images) {
        return execute(api.editImage(buildEditImageBody(request, mask, images)));
    }

    /**
     * Builds the multipart body for an image edit. Shared by the blocking and
     * streaming paths so a field can never be wired into only one of them.
     */
    private MultipartBody buildEditImageBody(EditImageRequest request, java.io.File mask, List<java.io.File> images) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MediaType.get("multipart/form-data"))
                .addFormDataPart("prompt", request.getPrompt());
        // 添加多个图像
        if (images.size() == 1) {
            RequestBody imageBody = RequestBody.create(images.get(0), MediaType.parse(getMediaTypeStr(images.get(0))));
            builder.addFormDataPart("image", images.get(0).getName(), imageBody);
        } else {
            for (java.io.File image : images) {
                RequestBody imageBody = RequestBody.create(image, MediaType.parse(getMediaTypeStr(image)));
                builder.addFormDataPart("image[]", image.getName(), imageBody);
            }
        }
        if (request.getSize() != null) {
            builder.addFormDataPart("size", request.getSize());
        }
        if (request.getResponseFormat() != null) {
            builder.addFormDataPart("response_format", request.getResponseFormat());
        }
        if (request.getN() != null) {
            builder.addFormDataPart("n", request.getN().toString());
        }
        if (mask != null) {
            RequestBody maskBody = RequestBody.create(mask, MediaType.parse("image/png"));
            builder.addFormDataPart("mask", "mask", maskBody);
        }
        if (request.getModel() != null) {
            builder.addFormDataPart("model", request.getModel());
        }
        if (request.getQuality() != null) {
            builder.addFormDataPart("quality", request.getQuality());
        }
        if (request.getBackground() != null) {
            builder.addFormDataPart("background", request.getBackground());
        }
        if (request.getOutputFormat() != null) {
            builder.addFormDataPart("output_format", request.getOutputFormat());
        }
        if (request.getOutputCompression() != null) {
            builder.addFormDataPart("output_compression", request.getOutputCompression().toString());
        }
        if (request.getInputFidelity() != null) {
            builder.addFormDataPart("input_fidelity", request.getInputFidelity());
        }
        if (request.getModeration() != null) {
            builder.addFormDataPart("moderation", request.getModeration());
        }
        if (request.getUser() != null) {
            builder.addFormDataPart("user", request.getUser());
        }
        if (request.getPartialImages() != null) {
            builder.addFormDataPart("partial_images", request.getPartialImages().toString());
        }
        if (Boolean.TRUE.equals(request.getStream())) {
            builder.addFormDataPart("stream", "true");
        }
        return builder.build();
    }

    /**
     * Stream image generation, emitting partial images as they are produced.
     * Set {@code partialImages} (0-3) on the request to control how many
     * partials arrive before the completed event.
     */
    public Flowable<ImageStreamEvent> streamCreateImage(CreateImageRequest request) {
        request.setStream(true);
        return eventSourceStream(api.streamCreateImage(request), ImageStreamEvent.class);
    }

    /**
     * Stream an image edit, emitting partial images as they are produced.
     */
    public Flowable<ImageStreamEvent> streamEditImage(EditImageRequest request, java.io.File image, java.io.File mask) {
        List<java.io.File> images = new ArrayList<>(1);
        images.add(image);
        return streamEditImage(request, mask, images);
    }

    /**
     * Stream an image edit over several input images.
     */
    public Flowable<ImageStreamEvent> streamEditImage(EditImageRequest request, java.io.File mask, List<java.io.File> images) {
        request.setStream(true);
        return eventSourceStream(api.streamEditImage(buildEditImageBody(request, mask, images)), ImageStreamEvent.class);
    }

    public ImageResponse imageVariation(ImageVariationRequest request, String imagePath) {
        java.io.File image = new java.io.File(imagePath);
        return imageVariation(request, image);
    }

    public ImageResponse imageVariation(ImageVariationRequest request, java.io.File image) {
        RequestBody imageBody = RequestBody.create(MediaType.parse("image"), image);

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MediaType.get("multipart/form-data"))
                .addFormDataPart("image", "image", imageBody);
        if (request.getSize() != null) {
            builder.addFormDataPart("size", request.getSize());
        }
        if (request.getResponseFormat() != null) {
            builder.addFormDataPart("response_format", request.getResponseFormat());
        }
        if (request.getN() != null) {
            builder.addFormDataPart("n", request.getN().toString());
        }
        if (request.getModel() != null) {
            builder.addFormDataPart("model", request.getModel());
        }
        if (request.getUser() != null) {
            builder.addFormDataPart("user", request.getUser());
        }
        return execute(api.imageVariation(builder.build()));
    }


    public VectorStore createVectorStore(VectorStoreRequest request) {
        return execute(api.createVectorStore(request));
    }

    public List<VectorStore> listVectorStores(ListRequest request) {
        Map<String, Object> queryParameters = mapper.convertValue(request, new TypeReference<Map<String, Object>>() {
        });
        return execute(api.listVectorStores(queryParameters)).data;
    }


    public VectorStore retrieveVectorStore(String vectorStoreId) {
        return execute(api.retrieveVectorStore(vectorStoreId));
    }

    public VectorStore modifyAssistant(String vectorStoreId, ModifyVectorStoreRequest request) {
        return execute(api.modifyVectorStore(vectorStoreId, request));
    }

    public DeleteStatus deleteVectorStore(String vectorStoreId) {
        return execute(api.deleteVectorStore(vectorStoreId));
    }

    public VectorStoreFile createVectorStoreFile(String vectorStoreId, VectorStoreFileRequest request) {
        return execute(api.createVectorStoreFile(vectorStoreId, request));
    }

    public List<VectorStoreFile> listVectorStoreFiles(String vectorStoreId, ListRequest request) {
        Map<String, Object> queryParameters = mapper.convertValue(request, new TypeReference<Map<String, Object>>() {
        });
        return execute(api.listVectorStoreFiles(vectorStoreId, queryParameters)).data;
    }

    public DeleteStatus deleteVectorStoreFile(String vectorStoreId, String fileId) {
        return execute(api.deleteVectorStoreFile(vectorStoreId, fileId));
    }


    public VectorStoreFileBatch createVectorStoreFileBatch(String vectorStoreId, VectorStoreFileBatchRequest request) {
        return execute(api.createVectorStoreFileBatch(vectorStoreId, request));
    }

    public VectorStoreFileBatch retrieveVectorStoreFileBatch(String vectorStoreId, String batchId) {
        return execute(api.retrieveVectorStoreFileBatch(vectorStoreId, batchId));
    }

    public VectorStoreFileBatch cancelVectorStoreFileBatch(String vectorStoreId, String batchId) {
        return execute(api.cancelVectorStoreFileBatch(vectorStoreId, batchId));
    }

    public List<VectorStoreFile> listVectorStoreFileInBatch(String vectorStoreId, String batchId, ListRequest request) {
        Map<String, Object> queryParameters = mapper.convertValue(request, new TypeReference<Map<String, Object>>() {
        });
        return execute(api.listVectorStoreFileInBatch(vectorStoreId, batchId, queryParameters)).data;
    }

    public RealtimeSession createRealtimeSession(CreateRealtimeSessionRequest request) {
        return execute(api.createRealtimeSession(request));
    }

    public Batch createBatch(CreateBatchRequest request) {
        return execute(api.createBatch(request));
    }

    public Batch retrieveBatch(String batchId) {
        return execute(api.retrieveBatch(batchId));
    }

    public Batch cancelBatch(String batchId) {
        return execute(api.cancelBatch(batchId));
    }

    public List<Batch> listBatch(String after, Integer limit) {
        return execute(api.listBatch(after, limit)).getData();
    }

    public VideoJob createVideo(CreateVideoRequest request, String imagePath) {
        java.io.File image = new java.io.File(imagePath);
        return createVideo(request, image);
    }
    
    public VideoJob createVideo(CreateVideoRequest request, java.io.File image) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MediaType.get("multipart/form-data"));

        if (image != null) {
            RequestBody imageBody = RequestBody.create(MediaType.parse("image"), image);
            builder.addFormDataPart("input_reference", image.getName(), imageBody);
        }

        builder.addFormDataPart("prompt", request.getPrompt());
        if (request.getModel() != null) {
            builder.addFormDataPart("model", request.getModel());
        }
        if (request.getSeconds() != null) {
            builder.addFormDataPart("seconds", request.getSeconds());
        }
        if (request.getSize() != null) {
            builder.addFormDataPart("size", request.getSize());
        }
        return execute(api.createVideo(builder.build()));
    }

    public VideoJob remixVideo(String videoId, RemixVideoRequest request) {
        return execute(api.remixVideo(videoId, request));
    }

    public List<VideoJob> listVideos(String after, Integer limit, String order) {
        return execute(api.listVideos(after, limit, order)).getData();
    }

    public VideoJob retrieveVideo(String videoId) {
        return execute(api.retrieveVideo(videoId));
    }

    public VideoJob deleteVideo(String videoId) {
        return execute(api.deleteVideo(videoId));
    }

    public ResponseBody retrieveVideoContent(String videoId, String variant) {
        return execute(api.retrieveVideoContent(videoId, variant));
    }

    // Responses API methods

    /**
     * Create a response using the Responses API.
     *
     * @param request The response request
     * @return The response object
     */
    public Response createResponse(ResponseRequest request) {
        request.setStream(false);
        return execute(api.createResponse(request));
    }

    /**
     * Generic EventSource-based SSE streaming method.
     * Uses OkHttp's native EventSource for robust SSE handling.
     *
     * @param apiCall The Retrofit Call object
     * @param cl      The class to deserialize each event to
     * @param <T>     The type of events
     * @return A Flowable of streaming events
     */
    public <T> Flowable<T> eventSourceStream(Call<ResponseBody> apiCall, Class<T> cl) {
        if (httpClient == null) {
            throw new IllegalStateException("Cannot use EventSource streaming with OpenaiApi-only constructor. Use a constructor that creates an OkHttpClient.");
        }

        return Flowable.create(emitter -> {
            try {
                // Get the request from Retrofit Call and clone it
                Request httpRequest = apiCall.request();

                // Create EventSource factory and listener
                EventSource.Factory factory = EventSources.createFactory(httpClient);

                EventSourceListener listener = new EventSourceListener() {
                    @Override
                    public void onOpen(EventSource eventSource, okhttp3.Response response) {
                        // Connection opened
                    }

                    @Override
                    public void onEvent(EventSource eventSource, String id, String type, String data) {
                        if (emitter.isCancelled()) {
                            eventSource.cancel();
                            return;
                        }

                        // Skip [DONE] marker
                        if ("[DONE]".equals(data)) {
                            return;
                        }

                        try {
                            T event = mapper.readValue(data, cl);
                            emitter.onNext(event);
                        } catch (Exception e) {
                            emitter.onError(e);
                        }
                    }

                    @Override
                    public void onClosed(EventSource eventSource) {
                        if (!emitter.isCancelled()) {
                            emitter.onComplete();
                        }
                    }

                    @Override
                    public void onFailure(EventSource eventSource, Throwable t, okhttp3.Response response) {
                        if (!emitter.isCancelled()) {
                            if (t != null) {
                                emitter.onError(t);
                            } else if (response != null) {
                                // Try to parse error body
                                try {
                                    ResponseBody errorBody = response.body();
                                    if (errorBody != null) {
                                        String errorStr = errorBody.string();
                                        ChatResponseError error = mapper.readValue(errorStr, ChatResponseError.class);
                                        emitter.onError(new VacSdkException(error.getError().getCode(), error.getError().getMessage(), error));
                                    } else {
                                        emitter.onError(new RuntimeException("SSE connection failed: " + response.code() + " " + response.message()));
                                    }
                                } catch (Exception e) {
                                    emitter.onError(new RuntimeException("SSE connection failed: " + response.code() + " " + response.message()));
                                }
                            } else {
                                emitter.onError(new RuntimeException("SSE connection failed: unknown error"));
                            }
                        }
                    }
                };

                // Create and start the EventSource
                EventSource eventSource = factory.newEventSource(httpRequest, listener);

                // Set up cancellation
                emitter.setCancellable(eventSource::cancel);

            } catch (Exception e) {
                emitter.onError(e);
            }
        }, BackpressureStrategy.BUFFER);
    }

    /**
     * Create a streaming response using the Responses API.
     * Uses OkHttp's native EventSource for robust SSE handling.
     *
     * @param request The response request
     * @return A Flowable of streaming events
     */
    public Flowable<ResponseStreamEvent> streamCreateResponse(ResponseRequest request) {
        request.setStream(true);
        return eventSourceStream(api.streamCreateResponse(request), ResponseStreamEvent.class);
    }

    /**
     * Retrieve a response by ID.
     *
     * @param responseId The response ID
     * @return The response object
     */
    public Response retrieveResponse(String responseId) {
        return execute(api.retrieveResponse(responseId));
    }

    /**
     * Delete a response by ID.
     *
     * @param responseId The response ID
     * @return The deleted response object
     */
    public Response deleteResponse(String responseId) {
        return execute(api.deleteResponse(responseId));
    }

    /**
     * List responses.
     *
     * @param after  Cursor for pagination
     * @param limit  Maximum number of responses to return
     * @param order  Sort order ("asc" or "desc")
     * @return List of responses
     */
    public List<Response> listResponses(String after, Integer limit, String order) {
        return execute(api.listResponses(after, limit, order)).getData();
    }

    /**
     * Cancel a response that is in progress.
     *
     * @param responseId The response ID
     * @return The cancelled response object
     */
    public Response cancelResponse(String responseId) {
        return execute(api.cancelResponse(responseId));
    }
}
