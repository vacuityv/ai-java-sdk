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
    
    public GeminiAuthenticationInterceptor() {
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .header("content-type", "application/json")
                .build();
        return chain.proceed(request);
    }
}
