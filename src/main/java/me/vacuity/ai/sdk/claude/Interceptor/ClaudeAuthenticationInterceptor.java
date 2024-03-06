package me.vacuity.ai.sdk.claude.Interceptor;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

/**
 * OkHttp Interceptor that adds an authorization token header
 */
public class ClaudeAuthenticationInterceptor implements Interceptor {

    private final String apiKey;

    public ClaudeAuthenticationInterceptor(String apiKey) {
        Objects.requireNonNull(apiKey, "Claude token required");
        this.apiKey = apiKey;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .build();
        return chain.proceed(request);
    }
}
