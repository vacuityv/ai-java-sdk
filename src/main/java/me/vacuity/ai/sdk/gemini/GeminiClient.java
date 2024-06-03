package me.vacuity.ai.sdk.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import me.vacuity.ai.sdk.gemini.Interceptor.GeminiAuthenticationInterceptor;
import me.vacuity.ai.sdk.gemini.api.GeminiApi;
import me.vacuity.ai.sdk.gemini.entity.ResponseBodyCallback;
import me.vacuity.ai.sdk.gemini.entity.SSE;
import me.vacuity.ai.sdk.gemini.error.ChatResponseError;
import me.vacuity.ai.sdk.gemini.exception.VacSdkException;
import me.vacuity.ai.sdk.gemini.request.ChatRequest;
import me.vacuity.ai.sdk.gemini.response.ChatResponse;
import me.vacuity.ai.sdk.gemini.response.StreamChatResponse;
import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.net.Proxy;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:10
 **/

public class GeminiClient {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com";

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final ObjectMapper mapper = defaultObjectMapper();

    private String apiKey;

    private final GeminiApi api;
    private final ExecutorService executorService;

    public GeminiClient(final String apiKey) {
        ObjectMapper mapper = defaultObjectMapper();
        OkHttpClient client = defaultClient(DEFAULT_TIMEOUT);
        Retrofit retrofit = defaultRetrofit(client, mapper, null);

        this.apiKey = apiKey;
        this.api = retrofit.create(GeminiApi.class);
        this.executorService = client.dispatcher().executorService();
    }

    public GeminiClient(final String apiKey, final Duration timeout) {
        ObjectMapper mapper = defaultObjectMapper();
        OkHttpClient client = defaultClient(timeout);
        Retrofit retrofit = defaultRetrofit(client, mapper, null);

        this.apiKey = apiKey;
        this.api = retrofit.create(GeminiApi.class);
        this.executorService = client.dispatcher().executorService();
    }

    public GeminiClient(final String apiKey, final Duration timeout, String baseUrl) {
        ObjectMapper mapper = defaultObjectMapper();
        OkHttpClient client = defaultClient(timeout);
        Retrofit retrofit = defaultRetrofit(client, mapper, baseUrl);

        this.apiKey = apiKey;
        this.api = retrofit.create(GeminiApi.class);
        this.executorService = client.dispatcher().executorService();
    }

    public GeminiClient(String apiKey, GeminiApi api) {
        this.apiKey = apiKey;
        this.api = api;
        this.executorService = null;
    }

    public GeminiClient(String apiKey, final Duration timeout, Proxy proxy) {
        ObjectMapper mapper = defaultObjectMapper();
        OkHttpClient httpClient = defaultClient(timeout)
                .newBuilder()
                .proxy(proxy)
                .build();
        Retrofit retrofit = defaultRetrofit(httpClient, mapper, null);
        this.apiKey = apiKey;
        this.api = retrofit.create(GeminiApi.class);
        this.executorService = httpClient.dispatcher().executorService();
    }

    public GeminiClient(String apiKey, final Duration timeout, Proxy proxy, String proxyUsername, String proxyPassword) {
        Authenticator proxyAuthenticator = (route, response) -> {
            String credential = Credentials.basic(proxyUsername, proxyPassword);
            return response.request().newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build();
        };
        ObjectMapper mapper = defaultObjectMapper();
        OkHttpClient httpClient = defaultClient(timeout)
                .newBuilder()
                .proxy(proxy)
                .proxyAuthenticator(proxyAuthenticator)
                .build();
        Retrofit retrofit = defaultRetrofit(httpClient, mapper, null);
        this.apiKey = apiKey;
        this.api = retrofit.create(GeminiApi.class);
        this.executorService = httpClient.dispatcher().executorService();
    }

    public GeminiClient(String apiKey, final Duration timeout, Proxy proxy, Authenticator proxyAuthenticator) {
        ObjectMapper mapper = defaultObjectMapper();
        OkHttpClient httpClient = defaultClient(timeout)
                .newBuilder()
                .proxy(proxy)
                .proxyAuthenticator(proxyAuthenticator)
                .build();
        Retrofit retrofit = defaultRetrofit(httpClient, mapper, null);
        this.apiKey = apiKey;
        this.api = retrofit.create(GeminiApi.class);
        this.executorService = httpClient.dispatcher().executorService();
    }

    public static ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }

    public static OkHttpClient defaultClient(Duration timeout) {
        return new OkHttpClient.Builder()
                .addInterceptor(new GeminiAuthenticationInterceptor())
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
                VacSdkException ve = new VacSdkException("-1", "error", Arrays.asList(error));
                throw ve;
            } catch (IOException ex) {
                // couldn't parse error
                throw e;
            }
        }
    }


    public ChatResponse chat(ChatRequest request) {
        return execute(api.chat(request.getModel(), this.apiKey, request));
    }

    public Flowable<StreamChatResponse> streamChat(ChatRequest request) {
        return stream(api.streamChat(request.getModel(), this.apiKey, request), StreamChatResponse.class);
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

}
