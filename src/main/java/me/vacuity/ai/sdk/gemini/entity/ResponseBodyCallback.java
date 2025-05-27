package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.FlowableEmitter;
import me.vacuity.ai.sdk.common.VacSdkException;
import me.vacuity.ai.sdk.gemini.GeminiClient;
import me.vacuity.ai.sdk.gemini.error.ChatResponseError;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Callback to parse Server Sent Events (SSE) from raw InputStream and
 * emit the events with io.reactivex.FlowableEmitter to allow streaming of
 * SSE.
 */
public class ResponseBodyCallback implements Callback<ResponseBody> {
    private static final ObjectMapper mapper = GeminiClient.defaultObjectMapper();
    
    private final FlowableEmitter<SSE> emitter;
    private StringBuilder jsonBuilder;
    private int squareBracketCount = 0;  // 方括号计数
    private int curlyBracketCount = 0;   // 花括号计数
    private boolean isFirstObject = true; // 标记是否是数组中的第一个对象

    public ResponseBodyCallback(FlowableEmitter<SSE> emitter, boolean emitDone) {
        this.emitter = emitter;
        this.jsonBuilder = new StringBuilder(1024); // Pre-size with reasonable capacity
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if (!response.isSuccessful()) {
            HttpException e = new HttpException(response);
            ResponseBody errorBody = response.errorBody();

            if (errorBody == null) {
                throw e;
            } else {
                ChatResponseError error = null;
                try {
                    error = mapper.readValue(
                            errorBody.string(),
                            ChatResponseError.class
                    );
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                throw new VacSdkException(error.getError().getCode(), error.getError().getMessage(), error);
            }
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8))) {

            String line;
            while (!emitter.isCancelled() && (line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                processLine(line);
            }

            emitter.onComplete();

        } catch (Throwable t) {
            onFailure(call, t);
        }
    }

    private void processLine(String line) {
        try {
            boolean insideString = false; // 标记是否在字符串内
            boolean escapeNextChar = false; // 标记下一个字符是否是转义字符
            // 计算括号匹配
            for (char c : line.toCharArray()) {
                if (escapeNextChar) {
                    // 如果前一个字符是反斜杠，忽略当前字符，继续处理下一个字符
                    escapeNextChar = false;
                    continue;
                }

                if (c == '\\') {
                    // 如果遇到反斜杠，标记下一个字符为转义字符
                    escapeNextChar = true;
                } else if (c == '"') {
                    // 如果遇到引号，判断是否在字符串内
                    insideString = !insideString;
                }
                // 仅在不在字符串内时计算括号
                if (!insideString) {
                    switch (c) {
                        case '[':
                            squareBracketCount++;
                            break;
                        case ']':
                            squareBracketCount--;
                            break;
                        case '{':
                            curlyBracketCount++;
                            break;
                        case '}':
                            curlyBracketCount--;
                            break;
                    }
                }
            }

            // 处理开始的 "[" 或 "[{"
            if (jsonBuilder.length() == 0 && line.startsWith("[")) {
                jsonBuilder.append(line);
                return;
            }

            // 处理结束的 "]" 或 "}]"
            if (line.endsWith("]") && squareBracketCount == 0) {
                return;
            }

            // 处理对象分隔符 ","
            if (line.equals(",")) {
                isFirstObject = false;
                jsonBuilder = new StringBuilder(1024); // Reset with pre-sized capacity
                return;
            }

            jsonBuilder.append(line);

            // 当花括号匹配且不是数组的结束时，可能是一个完整的对象
            if (curlyBracketCount == 0 && squareBracketCount > 0) {
                String currentJson = jsonBuilder.toString();
                if (currentJson.endsWith("}")) {
                    // 提取当前对象
                    String objectJson;
                    if (isFirstObject) {
                        objectJson = currentJson.substring(1); // 去掉开头的 "["
                    } else {
                        objectJson = currentJson;
                    }
                    processJsonObject(objectJson);
                }
            }

        } catch (Exception e) {
            emitter.onError(e);
        }
    }

    private void processJsonObject(String json) throws IOException {
        emitter.onNext(new SSE(json));
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        emitter.onError(t);
    }
}
