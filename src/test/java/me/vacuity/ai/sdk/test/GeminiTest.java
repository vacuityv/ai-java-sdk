package me.vacuity.ai.sdk.test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Flowable;
import lombok.SneakyThrows;
import me.vacuity.ai.sdk.gemini.GeminiClient;
import me.vacuity.ai.sdk.gemini.api.GeminiApi;
import me.vacuity.ai.sdk.gemini.entity.ChatFunction;
import me.vacuity.ai.sdk.gemini.entity.ChatFunctionCall;
import me.vacuity.ai.sdk.gemini.entity.ChatMessage;
import me.vacuity.ai.sdk.gemini.entity.DynamicRetrievalConfig;
import me.vacuity.ai.sdk.gemini.entity.GoogleSearchRetrieval;
import me.vacuity.ai.sdk.gemini.entity.Tool;
import me.vacuity.ai.sdk.gemini.enums.HarmBlockThreshold;
import me.vacuity.ai.sdk.gemini.enums.HarmCategory;
import me.vacuity.ai.sdk.gemini.exception.VacSdkException;
import me.vacuity.ai.sdk.gemini.request.ChatRequest;
import me.vacuity.ai.sdk.gemini.response.ChatResponse;
import me.vacuity.ai.sdk.gemini.response.ChatResponseCandidate;
import me.vacuity.ai.sdk.gemini.response.StreamChatResponse;
import me.vacuity.ai.sdk.gemini.service.FunctionExecutor;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import retrofit2.Retrofit;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.vacuity.ai.sdk.gemini.GeminiClient.defaultClient;
import static me.vacuity.ai.sdk.gemini.GeminiClient.defaultObjectMapper;
import static me.vacuity.ai.sdk.gemini.GeminiClient.defaultRetrofit;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 11:14
 **/

public class GeminiTest {

    public static final String API_KEY = "*****";


    @Test
    public void chat() {

        FunctionExecutor functionExecutor = new FunctionExecutor(Collections.singletonList(ChatFunction.builder()
                .name("get_weather")
                .description("get the weather value of a city on a date")
                .executor(WeatherReq.class, w -> new WeatherResponse(true, "sunny"))
                .build()));

        List<ChatFunction> functionDeclarations = functionExecutor.getFunctions();

        List<Tool> tools = new ArrayList<>();
        // ⚠️ YOU CAN NOT USE BOTH FUNCTION AND CODE_EXCUTION
        tools.add(Tool.builder()
                .functionDeclarations(functionDeclarations)
                .build());
//        tools.add(Tool.builder()
//                .codeExcution(CodeExcution.builder().build())
//                .build());
        
        GeminiClient client = new GeminiClient(API_KEY);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "introduce yourself pls"));

        List<ChatRequest.SafetySetting> safetySettings = new ArrayList<>();
        safetySettings.add(new ChatRequest.SafetySetting(HarmCategory.HARM_CATEGORY_HATE_SPEECH.toString(), HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE.toString()));

        Map<String, Object> thinkingConfig = new HashMap<>();
        thinkingConfig.put("include_thoughts", Boolean.TRUE);
        
        ChatRequest.GenerationConfig config = ChatRequest.GenerationConfig.builder().build();
        config.setThinkingConfig(thinkingConfig);
        
        ChatRequest request = ChatRequest.builder()
                .model("gemini-2.0-flash-thinking-exp-01-21")
                .contents(messages)
                .safetySettings(safetySettings)
//                .tools(tools)
//                .generationConfig(config)
                .build();
        try {
            ChatResponse response = client.chat(request);
            System.out.println(response);
        } catch (VacSdkException e) {
            if (e.getDetails() != null) {
                System.out.println(e.getDetails().get(0).getError().getMessage());
            }
        }
    }

    @Test
    public void streamChat() throws JsonProcessingException {
        FunctionExecutor functionExecutor = new FunctionExecutor(Collections.singletonList(ChatFunction.builder()
                .name("get_weather")
                .description("get the weather value of a city on a date")
                .executor(WeatherReq.class, w -> {
                    if (!w.city.equals("xiamen") || !w.date.equals("2024-08-10")) {
                        return new WeatherResponse(false, "city or date not supported");
                    }
                    return new WeatherResponse(true, "sunny");
                })
                .build()));

        List<ChatFunction> functionDeclarations = functionExecutor.getFunctions();

        List<Tool> tools = new ArrayList<>();
        // ⚠️ YOU CAN NOT USE BOTH FUNCTION AND CODE_EXCUTION
        tools.add(Tool.builder()
                .functionDeclarations(functionDeclarations)
                .build());
//        tools.add(Tool.builder()
//                .codeExcution(CodeExcution.builder().build())
//                .build());

        GeminiClient client = new GeminiClient(API_KEY);
//        GeminiClient client = new GeminiClient(API_KEY, Duration.ofMinutes(1), BASE);
        
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "what's the weather of amoy on 2024-08-10"));
//        messages.add(new ChatMessage("user", "你好，用500字详细介绍下你自己"));
        ChatRequest request = ChatRequest.builder()
//                .model("gemini-2.0-flash-exp")
                .model("gemini-1.5-pro-latest")
                .tools(tools)
                .contents(messages)
                .build();
        System.out.println(defaultObjectMapper().writeValueAsString(request));
        System.out.println("ans:\n\n");
        try {
            AtomicBoolean goon = new AtomicBoolean(false);
            Flowable<StreamChatResponse> response = client.streamChat(request);
            response.doOnNext(s -> {
                System.out.println(s);
                ChatResponseCandidate candidate = s.getCandidates().get(0);
                
                ChatFunctionCall call = candidate.getContent().getParts().get(0).getFunctionCall();
                if (call != null) {
                    goon.set(true);
                    messages.add(candidate.getContent());
                    ChatMessage message = functionExecutor.executeAndConvertToMessage(call);
                    messages.add(message);
                    request.setContents(messages);
                }
            }).blockingSubscribe();
            if (goon.get()) {
                Flowable<StreamChatResponse> response2 = client.streamChat(request);
                System.out.println(defaultObjectMapper().writeValueAsString(request));
                response2.doOnNext(s -> {
                    System.out.println(s);
                }).blockingSubscribe();
            }
        } catch (VacSdkException e) {
            if (e.getDetails() != null) {
                System.out.println(e.getDetails().get(0).getError().getMessage());
            }
        }
    }

    @Test
    public void proxyChat() {
        String host = "127.0.0.1";
        int port = 7890;
        ObjectMapper mapper = defaultObjectMapper();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        OkHttpClient httpClient = defaultClient(Duration.ofSeconds(60))
                .newBuilder()
                .proxy(proxy)
                .build();
        Retrofit retrofit = defaultRetrofit(httpClient, mapper, null);
        GeminiApi api = retrofit.create(GeminiApi.class);
        GeminiClient client = new GeminiClient(API_KEY, api);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "introduce yourself please"));
        ChatRequest request = ChatRequest.builder()
                .contents(messages)
                .build();
        try {
            ChatResponse response = client.chat(request);
            System.out.println(response.getCandidates().get(0).getContent().getParts().get(0).getText());
        } catch (VacSdkException e) {
            if (e.getDetails() != null) {
                System.out.println(e.getDetails().get(0).getError().getMessage());
            }
        }
    }

    @Test
    public void testSystemInstruction() {
        GeminiClient client = new GeminiClient(API_KEY);
        ChatMessage systemInstruction = new ChatMessage("You are a assistant. Your name is KOI, you can help me with my daily work.");

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "what's your name"));

        List<ChatRequest.SafetySetting> safetySettings = new ArrayList<>();
        safetySettings.add(new ChatRequest.SafetySetting(HarmCategory.HARM_CATEGORY_HATE_SPEECH.toString(), HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE.toString()));

        ChatRequest request = ChatRequest.builder()
                .model("gemini-1.5-flash")
                .contents(messages)
                .safetySettings(safetySettings)
                .systemInstruction(systemInstruction)
                .build();
        try {
            ChatResponse response = client.chat(request);
            System.out.println(response);
        } catch (VacSdkException e) {
            if (e.getDetails() != null) {
                System.out.println(e.getDetails().get(0).getError().getMessage());
            }
        }
    }

    public static class WeatherReq {

        public String city;

        @JsonProperty(required = true)
        public String date;
    }

    public static class WeatherResponse {

        private boolean success;
        
        public String value;

        public WeatherResponse(boolean success, String value) {
            this.success = success;
            this.value = value;
        }
    }

    @SneakyThrows
    @Test
    public void chatWithSearch() {


        List<Tool> tools = new ArrayList<>();
        tools.add(Tool.builder()
                .googleSearchRetrieval(GoogleSearchRetrieval.builder()
                        .dynamicRetrievalConfig(DynamicRetrievalConfig.builder()
                                .build())
                        .build())
                .build());

        GeminiClient client = new GeminiClient(API_KEY);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "2028年奥运会在哪举办"));

        List<ChatRequest.SafetySetting> safetySettings = new ArrayList<>();
        safetySettings.add(new ChatRequest.SafetySetting(HarmCategory.HARM_CATEGORY_HATE_SPEECH.toString(), HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE.toString()));

        Map<String, Object> thinkingConfig = new HashMap<>();
        thinkingConfig.put("include_thoughts", Boolean.TRUE);

        ChatRequest.GenerationConfig config = ChatRequest.GenerationConfig.builder().build();
        config.setThinkingConfig(thinkingConfig);

        ChatRequest request = ChatRequest.builder()
                .model("gemini-1.5-pro-002")
                .contents(messages)
                .safetySettings(safetySettings)
                .tools(tools)
//                .generationConfig(config)
                .build();
        try {

            System.out.println(defaultObjectMapper().writeValueAsString(request));
            
            ChatResponse response = client.chat(request);
            System.out.println(response);
        } catch (VacSdkException e) {
            if (e.getDetails() != null) {
                System.out.println(e.getDetails().get(0).getError().getMessage());
            }
        }
    }
}
