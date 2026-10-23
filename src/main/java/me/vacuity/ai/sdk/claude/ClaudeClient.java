package me.vacuity.ai.sdk.claude;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import me.vacuity.ai.sdk.claude.Interceptor.ClaudeAuthenticationInterceptor;
import me.vacuity.ai.sdk.claude.api.ClaudeApi;
import me.vacuity.ai.sdk.claude.entity.ChatFunction;
import me.vacuity.ai.sdk.claude.entity.ChatFunctionMixIn;
import me.vacuity.ai.sdk.claude.entity.ResponseBodyCallback;
import me.vacuity.ai.sdk.claude.entity.SSE;
import me.vacuity.ai.sdk.claude.error.ChatResponseError;
import me.vacuity.ai.sdk.claude.request.ChatRequest;
import me.vacuity.ai.sdk.claude.response.ChatResponse;
import me.vacuity.ai.sdk.claude.response.StreamChatResponse;
import me.vacuity.ai.sdk.common.VacSdkException;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:10
 **/

public class ClaudeClient {

    private static final String BASE_URL = "https://api.anthropic.com";

    /**
     * API version sent by default. Switched from 2023-06-01 in 1.14.0.
     * <p>
     * Under this version the Messages API rejects temperature / top_p / top_k
     * outright, even at their former default values. Pass
     * {@link #LEGACY_ANTHROPIC_VERSION} to a version-taking constructor if you
     * depend on non-default sampling.
     */
    public static final String DEFAULT_ANTHROPIC_VERSION = "2026-10-21";

    /**
     * The version this SDK sent before 1.14.0. Still accepted by the API and
     * not deprecated, so it remains a valid escape hatch.
     */
    public static final String LEGACY_ANTHROPIC_VERSION = "2023-06-01";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final ObjectMapper mapper = defaultObjectMapper();


    private final ClaudeApi api;
    private final ExecutorService executorService;
    private final String anthropicVersion;

    public ClaudeClient(final String token) {
        OkHttpClient client = defaultClient(token, DEFAULT_TIMEOUT);
        Retrofit retrofit = defaultRetrofit(client, mapper, null);

        this.api = retrofit.create(ClaudeApi.class);
        this.executorService = client.dispatcher().executorService();
        this.anthropicVersion = DEFAULT_ANTHROPIC_VERSION;
    }

    public ClaudeClient(final String token, final Duration timeout) {
        OkHttpClient client = defaultClient(token, timeout);
        Retrofit retrofit = defaultRetrofit(client, mapper, null);

        this.api = retrofit.create(ClaudeApi.class);
        this.executorService = client.dispatcher().executorService();
        this.anthropicVersion = DEFAULT_ANTHROPIC_VERSION;
    }

    public ClaudeClient(final String token, final Duration timeout, String baseUrl) {
        OkHttpClient client = defaultClient(token, timeout);
        Retrofit retrofit = defaultRetrofit(client, mapper, baseUrl);

        this.api = retrofit.create(ClaudeApi.class);
        this.executorService = client.dispatcher().executorService();
        this.anthropicVersion = DEFAULT_ANTHROPIC_VERSION;
    }

    public ClaudeClient(ClaudeApi api) {
        this.api = api;
        this.executorService = null;
        this.anthropicVersion = DEFAULT_ANTHROPIC_VERSION;
    }

    /**
     * Pins the anthropic-version header, e.g. {@link #LEGACY_ANTHROPIC_VERSION}
     * to keep sending non-default temperature / top_p / top_k.
     */
    public ClaudeClient(final String token, final Duration timeout, String baseUrl,
                        String anthropicVersion) {
        OkHttpClient client = defaultClient(token, timeout, anthropicVersion);
        Retrofit retrofit = defaultRetrofit(client, mapper, baseUrl);

        this.api = retrofit.create(ClaudeApi.class);
        this.executorService = client.dispatcher().executorService();
        this.anthropicVersion = anthropicVersion == null
                ? DEFAULT_ANTHROPIC_VERSION : anthropicVersion;
    }

    public ClaudeClient(final String token, final Duration timeout, Proxy proxy) {
        OkHttpClient httpClient = defaultClient(token, timeout)
                .newBuilder()
                .proxy(proxy)
                .build();
        Retrofit retrofit = defaultRetrofit(httpClient, mapper, null);
        this.api = retrofit.create(ClaudeApi.class);
        this.executorService = httpClient.dispatcher().executorService();
        this.anthropicVersion = DEFAULT_ANTHROPIC_VERSION;
    }

    public ClaudeClient(final String token, final Duration timeout, Proxy proxy, String proxyUsername, String proxyPassword) {
        Authenticator proxyAuthenticator = (route, response) -> {
            String credential = Credentials.basic(proxyUsername, proxyPassword);
            return response.request().newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build();
        };
        OkHttpClient httpClient = defaultClient(token, timeout)
                .newBuilder()
                .proxy(proxy)
                .proxyAuthenticator(proxyAuthenticator)
                .build();
        Retrofit retrofit = defaultRetrofit(httpClient, mapper, null);
        this.api = retrofit.create(ClaudeApi.class);
        this.executorService = httpClient.dispatcher().executorService();
        this.anthropicVersion = DEFAULT_ANTHROPIC_VERSION;
    }

    public ClaudeClient(final String token, final Duration timeout, Proxy proxy, Authenticator proxyAuthenticator) {
        OkHttpClient httpClient = defaultClient(token, timeout)
                .newBuilder()
                .proxy(proxy)
                .proxyAuthenticator(proxyAuthenticator)
                .build();
        Retrofit retrofit = defaultRetrofit(httpClient, mapper, null);
        this.api = retrofit.create(ClaudeApi.class);
        this.executorService = httpClient.dispatcher().executorService();
        this.anthropicVersion = DEFAULT_ANTHROPIC_VERSION;
    }


    public static ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.addMixIn(ChatFunction.class, ChatFunctionMixIn.class);
        return mapper;
    }

    public static OkHttpClient defaultClient(String apiKey, Duration timeout) {
        return defaultClient(apiKey, timeout, DEFAULT_ANTHROPIC_VERSION);
    }

    public static OkHttpClient defaultClient(String apiKey, Duration timeout, String anthropicVersion) {
        return new OkHttpClient.Builder()
                .addInterceptor(new ClaudeAuthenticationInterceptor(apiKey, anthropicVersion))
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
                ChatResponseError error = mapper.readValue(errorBody, ChatResponseError.class);
                VacSdkException ve = new VacSdkException(error.getError().getType(), error.getError().getMessage(), error);
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

    public ChatResponse chat(ChatRequest request) {
        rejectRemovedSamplingParameters(request);
        String betas = betaHeader(request);
        return betas == null ? execute(api.chat(request)) : execute(api.chat(betas, request));
    }

    public Flowable<StreamChatResponse> streamChat(ChatRequest request) {
        rejectRemovedSamplingParameters(request);
        request.setStream(true);
        String betas = betaHeader(request);
        Call<ResponseBody> call = betas == null ? api.streamChat(request) : api.streamChat(betas, request);
        return stream(call, StreamChatResponse.class);
    }

    /**
     * API version 2026-10-21 rejects temperature / top_p / top_k with a 400,
     * even at their former defaults. Failing here instead means the caller
     * finds out at the call site rather than from a server error, and the
     * message says what to do about it.
     */
    private void rejectRemovedSamplingParameters(ChatRequest request) {
        if (LEGACY_ANTHROPIC_VERSION.equals(anthropicVersion) || request == null) {
            return;
        }
        String offender = null;
        if (request.getTemperature() != null) {
            offender = "temperature";
        } else if (request.getTopP() != null) {
            offender = "top_p";
        } else if (request.getTopK() != null) {
            offender = "top_k";
        }
        if (offender != null) {
            throw new IllegalArgumentException(
                    "anthropic-version " + anthropicVersion + " removed sampling parameters: "
                            + offender + " is rejected with a 400 even at its former default. "
                            + "Remove it from the request, or construct ClaudeClient with "
                            + "ClaudeClient.LEGACY_ANTHROPIC_VERSION to keep using it.");
        }
    }

    /**
     * Builds the anthropic-beta header from the request, or returns null to
     * fall back to the legacy default header.
     */
    private static String betaHeader(ChatRequest request) {
        List<String> betas = request.getBetas();
        if (betas == null || betas.isEmpty()) {
            return null;
        }
        return String.join(",", betas);
    }

}
