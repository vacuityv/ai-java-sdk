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

    /**
     * Value sent in the anthropic-version header. Set via the constructor so a
     * caller can pin an older version.
     */
    private final String anthropicVersion;

    public ClaudeAuthenticationInterceptor(String apiKey) {
        this(apiKey, me.vacuity.ai.sdk.claude.ClaudeClient.DEFAULT_ANTHROPIC_VERSION);
    }

    public ClaudeAuthenticationInterceptor(String apiKey, String anthropicVersion) {
        Objects.requireNonNull(apiKey, "Claude token required");
        this.apiKey = apiKey;
        this.anthropicVersion = anthropicVersion == null
                ? me.vacuity.ai.sdk.claude.ClaudeClient.DEFAULT_ANTHROPIC_VERSION
                : anthropicVersion;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .header("x-api-key", apiKey)
                .header("anthropic-version", anthropicVersion)
                .header("content-type", "application/json")
                .build();
        return chain.proceed(request);
    }
}
