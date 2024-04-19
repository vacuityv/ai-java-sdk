package me.vacuity.ai.sdk.openai.assistant.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.FlowableEmitter;
import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.error.ChatResponseError;
import me.vacuity.ai.sdk.openai.exception.VacSdkException;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Callback to parse Server Sent Events (SSE) from raw InputStream and
 * emit the events with io.reactivex.FlowableEmitter to allow streaming of
 * SSE.
 */
public class AssistantResponseBodyCallback implements Callback<ResponseBody> {
    private static final ObjectMapper mapper = OpenaiClient.defaultObjectMapper();

    private final FlowableEmitter<AssistantSSE> emitter;

    public AssistantResponseBodyCallback(FlowableEmitter<AssistantSSE> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        BufferedReader reader = null;

        try {
            if (!response.isSuccessful()) {
                HttpException e = new HttpException(response);
                ResponseBody errorBody = response.errorBody();

                if (errorBody == null) {
                    throw e;
                } else {
                    ChatResponseError error = mapper.readValue(
                            errorBody.string(),
                            ChatResponseError.class
                    );
                    throw new VacSdkException("-1", "stream error", error);
                }
            }

            InputStream in = response.body().byteStream();
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            AssistantSSE sse = null;
            String event = null;
            while (!emitter.isCancelled() && (line = reader.readLine()) != null) {
                if (line.startsWith("event:")) {
                    event = line.substring(7).trim();
                } else if (line.startsWith("data:")) {
                    String data = line.substring(6).trim();
                    sse = new AssistantSSE(event, data);
                } else if (line.equals("") && sse != null) {
                    emitter.onNext(sse);
                    sse = null;
                } else {
                    throw new VacSdkException("-1", "Invalid sse format! " + line);
                }
            }
            emitter.onComplete();
        } catch (Throwable t) {
            onFailure(call, t);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        emitter.onError(t);
    }
}
