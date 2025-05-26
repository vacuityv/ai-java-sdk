package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.FlowableEmitter;
import me.vacuity.ai.sdk.common.VacSdkException;
import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.error.ChatResponseError;
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


public class ResponseBodyCallback implements Callback<ResponseBody> {
    private static final ObjectMapper mapper = OpenaiClient.defaultObjectMapper();
    private static final int MAX_LINE_LENGTH = 64 * 1024; // 64KB per line limit
    
    private final FlowableEmitter<SSE> emitter;
    private final boolean emitDone;

    public ResponseBodyCallback(FlowableEmitter<SSE> emitter, boolean emitDone) {
        this.emitter = emitter;
        this.emitDone = emitDone;
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
                    throw new VacSdkException(error.getError().getCode(), error.getError().getMessage(), error);
                }
            }

            InputStream in = response.body().byteStream();
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            SSE sse = null;

            while (!emitter.isCancelled() && (line = reader.readLine()) != null) {
                // Check line length limit
                if (line.length() > MAX_LINE_LENGTH) {
                    emitter.onError(new IOException("Line too long: " + line.length() + " bytes"));
                    return;
                }
                
                if (line.startsWith("data:")) {
                    String data = line.substring(5).trim();
                    sse = new SSE(data);
                } else if (line.equals("") && sse != null) {
                    if (sse.isDone()) {
                        if (emitDone) {
                            emitter.onNext(sse);
                        }
                        break;
                    }
                    emitter.onNext(sse);
                    sse = null;
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
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        emitter.onError(t);
    }
}
