package me.vacuity.ai.sdk.gemini.Interceptor;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

/**
 * OkHttp Interceptor that adds an authorization token header
 */
public class GeminiAuthenticationInterceptor implements Interceptor {

    private final String apiKey;

    public GeminiAuthenticationInterceptor(String apiKey) {
        Objects.requireNonNull(apiKey, "apiKey required");
        this.apiKey = apiKey;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .header("content-type", "application/json")
                .header("x-goog-api-key", apiKey)
                .build();
        return chain.proceed(request);
    }
}
