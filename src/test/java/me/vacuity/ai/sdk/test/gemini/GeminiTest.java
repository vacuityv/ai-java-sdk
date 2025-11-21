package me.vacuity.ai.sdk.test.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import io.reactivex.Flowable;
import lombok.SneakyThrows;
import me.vacuity.ai.sdk.claude.ClaudeClient;
import me.vacuity.ai.sdk.common.VacSdkException;
import me.vacuity.ai.sdk.gemini.GeminiClient;
import me.vacuity.ai.sdk.gemini.entity.ChatFunction;
import me.vacuity.ai.sdk.gemini.entity.ChatFunctionCall;
import me.vacuity.ai.sdk.gemini.entity.ChatMessage;
import me.vacuity.ai.sdk.gemini.entity.ChatMessageContentPart;
import me.vacuity.ai.sdk.gemini.entity.Tool;
import me.vacuity.ai.sdk.gemini.enums.HarmBlockThreshold;
import me.vacuity.ai.sdk.gemini.enums.HarmCategory;
import me.vacuity.ai.sdk.gemini.enums.Modality;
import me.vacuity.ai.sdk.gemini.request.ChatRequest;
import me.vacuity.ai.sdk.gemini.response.ChatResponse;
import me.vacuity.ai.sdk.gemini.response.StreamChatResponse;
import me.vacuity.ai.sdk.gemini.service.FunctionExecutor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static me.vacuity.ai.sdk.gemini.GeminiClient.defaultObjectMapper;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 11:14
 **/

public class GeminiTest {

    public static final String API_KEY = System.getenv("GEMINI_API_KEY");

    public static final String URL = "https://chat.vacuity.me/gemini/";

    // 天气查询参数类
    static class WeatherParams {
        @JsonProperty("location")
        @JsonSchemaDescription("城市名称或地点")
        public String location;
        @JsonProperty("unit")
        @JsonSchemaDescription("温度单位，可选值：celsius(摄氏度) 或 fahrenheit(华氏度)")
        public String unit;
    }

    // 计算器参数类
    static class CalculatorParams {
        @JsonProperty("operation")
        @JsonSchemaDescription("数学运算类型：add(加法), subtract(减法), multiply(乘法), divide(除法)")
        public String operation;
        @JsonProperty("num1")
        @JsonSchemaDescription("第一个数字")
        public Double num1;
        @JsonProperty("num2")
        @JsonSchemaDescription("第二个数字")
        public Double num2;
    }


    @SneakyThrows
    @Test
    public void generateImg() {

        List<Tool> tools = new ArrayList<>();
        tools.add(Tool.builder()
                .googleSearch(new HashMap<>())
                .build()
        );

        GeminiClient client = new GeminiClient(API_KEY);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "画一写实的马"));

        List<ChatRequest.SafetySetting> safetySettings = new ArrayList<>();
        safetySettings.add(new ChatRequest.SafetySetting(HarmCategory.HARM_CATEGORY_HATE_SPEECH.toString(), HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE.toString()));

        List<Modality> responseModalities = new ArrayList<>();
        responseModalities.add(Modality.TEXT);
        responseModalities.add(Modality.IMAGE);

        ChatRequest.GenerationConfig config = ChatRequest.GenerationConfig.builder().build();
        config.setResponseModalities(responseModalities);

        ChatRequest request = ChatRequest.builder()
                .model("gemini-2.0-flash-exp")
                .contents(messages)
                .safetySettings(safetySettings)
//                .tools(tools)
                .generationConfig(config)
                .build();
        try {

            System.out.println(defaultObjectMapper().writeValueAsString(request));

            ChatResponse response = client.chat(request);

            List<ChatMessageContentPart> parts = response.getCandidates().get(0).getContent().getParts();
            for (ChatMessageContentPart part : parts) {
                if (part.getInlineData() != null) {
                    System.out.println(part.getInlineData().getMimeType());
                }
                if (part.getText() != null) {
                    System.out.println(part.getText());
                }
            }

        } catch (VacSdkException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void streamChat() {
        GeminiClient client = new GeminiClient(API_KEY, Duration.ofSeconds(120), URL);
        ChatMessage chatMessage = new ChatMessage("user", "帮我画一匹马，水墨风格");
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(chatMessage);

        ChatRequest.GenerationConfig config = ChatRequest.GenerationConfig.builder().build();
        List<Modality> responseModalities = new ArrayList<>();
        responseModalities.add(Modality.TEXT);
        responseModalities.add(Modality.IMAGE);
        config.setResponseModalities(responseModalities);

        ChatRequest request = ChatRequest.builder()
                .contents(messages)
                .model("gemini-3-pro-image-preview")
                .generationConfig(config)
                .build();
        StringBuilder ss = new StringBuilder();
        Flowable<StreamChatResponse> response = client.streamChat(request);
        response.doOnNext(s -> {
            ss.append(s);
        }).blockingSubscribe();

        // 保存响应结果到文件
        try (FileWriter writer = new FileWriter("stream_gemini_response.json")) {
            writer.write(ss.toString());
            System.out.println("Response saved to gemini_response.json");
        } catch (IOException e) {
            System.err.println("Failed to save response to file: " + e.getMessage());
        }

    }

    @Test
    public void chat() {
        GeminiClient client = new GeminiClient(API_KEY);
        ChatMessage chatMessage = new ChatMessage("user", "帮我画一匹马，水墨风格");
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(chatMessage);

        ChatRequest.GenerationConfig config = ChatRequest.GenerationConfig.builder().build();
        List<Modality> responseModalities = new ArrayList<>();
        responseModalities.add(Modality.TEXT);
        responseModalities.add(Modality.IMAGE);
        config.setResponseModalities(responseModalities);

        ChatRequest request = ChatRequest.builder()
                .contents(messages)
                .model("gemini-2.5-flash-image-preview")
                .generationConfig(config)
                .build();
        ChatResponse response = client.chat(request);

        System.out.println(response);
    }


    @Test
    public void testDifferentModelsAndParams() {
        // 测试1: 纯文本请求 - 不要求图片
        System.out.println("========== 测试1: 纯文本模式 ==========");
        testStreamRequest("gemini-2.5-flash", "你好，请简单介绍一下自己", false);

        // 测试2: 不同的图片模型
        System.out.println("\n========== 测试2: 图片模式 - gemini-2.0-flash-exp ==========");
        testStreamRequest("gemini-2.0-flash-exp", "帮我画一匹马，水墨风格", true);

        // 测试3: 稳定版本
        System.out.println("\n========== 测试3: 图片模式 - gemini-1.5-flash ==========");
        testStreamRequest("gemini-1.5-flash", "帮我画一匹马，水墨风格", true);
    }

    private void testStreamRequest(String model, String prompt, boolean includeImage) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            String modalities = includeImage ? "[\"TEXT\", \"IMAGE\"]" : "[\"TEXT\"]";

            String requestBody = "{\n" +
                    "  \"contents\": [\n" +
                    "    {\n" +
                    "      \"parts\": [\n" +
                    "        {\n" +
                    "          \"text\": \"" + prompt + "\"\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"role\": \"user\"\n" +
                    "    }\n" +
                    "  ]" +
                    (includeImage ? ",\n  \"generationConfig\": {\n    \"responseModalities\": " + modalities + "\n  }" : "") +
                    "\n}";

            String url = "https://generativelanguage.googleapis.com/v1alpha/models/" + model + ":streamGenerateContent?key=" + API_KEY;

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MediaType.parse("application/json"), requestBody))
                    .addHeader("Content-Type", "application/json")
                    .build();

            System.out.println("模型: " + model);
            System.out.println("请求: " + prompt);
            System.out.println("包含图片: " + includeImage);

            okhttp3.Response response = client.newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                String rawResponse = response.body().string();
                System.out.println("响应长度: " + rawResponse.length() + " 字符");

                // 检查是否包含图片数据
                boolean hasImageData = rawResponse.contains("inlineData") && rawResponse.contains("image/");
                System.out.println("是否包含图片数据: " + hasImageData);

                // 统计响应对象数量
                int objectCount = rawResponse.split("\"candidates\"").length - 1;
                System.out.println("响应对象数量: " + objectCount);

                // 保存到文件
                String filename = "test_" + model.replace(".", "_").replace("-", "_") +
                        "_" + (includeImage ? "image" : "text") + ".txt";
                try (FileWriter writer = new FileWriter(filename)) {
                    writer.write(rawResponse);
                    System.out.println("已保存到: " + filename);
                }

            } else {
                System.out.println("请求失败: " + response.code() + " - " + response.message());
            }

            response.close();

        } catch (Exception e) {
            System.out.println("测试失败: " + e.getMessage());
        }
    }

    @Test
    public void compareNonStreamResponse() {
        System.out.println("========== 对比非流式响应 ==========");
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            String requestBody = "{\n" +
                    "  \"contents\": [\n" +
                    "    {\n" +
                    "      \"parts\": [\n" +
                    "        {\n" +
                    "          \"text\": \"帮我画一匹马，水墨风格\"\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"role\": \"user\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"generationConfig\": {\n" +
                    "    \"responseModalities\": [\"TEXT\", \"IMAGE\"]\n" +
                    "  }\n" +
                    "}";

            // 注意这里是 generateContent 而不是 streamGenerateContent
            String url = "https://generativelanguage.googleapis.com/v1alpha/models/gemini-2.5-flash-image-preview:generateContent?key=" + API_KEY;

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MediaType.parse("application/json"), requestBody))
                    .addHeader("Content-Type", "application/json")
                    .build();

            okhttp3.Response response = client.newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                String rawResponse = response.body().string();
                System.out.println("非流式响应长度: " + rawResponse.length() + " 字符");

                boolean hasImageData = rawResponse.contains("inlineData") && rawResponse.contains("image/");
                System.out.println("非流式是否包含图片: " + hasImageData);

                try (FileWriter writer = new FileWriter("non_stream_response.txt")) {
                    writer.write(rawResponse);
                    System.out.println("非流式响应已保存到: non_stream_response.txt");
                }

            } else {
                System.out.println("非流式请求失败: " + response.code() + " - " + response.message());
            }

            response.close();

        } catch (Exception e) {
            System.out.println("非流式测试失败: " + e.getMessage());
        }
    }

    @Test
    public void testFunctionCallingNonStream() {
        System.out.println("========== 非流式函数调用测试 ==========");
        try {
            GeminiClient client = new GeminiClient(API_KEY, Duration.ofSeconds(120), URL);

            // 定义函数
            List<ChatFunction> functions = new ArrayList<>();
            ChatFunction weatherFunction = ChatFunction.builder()
                    .name("get_weather")
                    .description("获取指定地点的天气信息")
                    .executor(WeatherParams.class, params -> {
                        WeatherParams weatherParams = (WeatherParams) params;
                        String location = weatherParams.location;
                        String unit = weatherParams.unit != null ? weatherParams.unit : "celsius";
                        System.out.println("执行函数: get_weather");
                        System.out.println("参数 - 位置: " + location + ", 单位: " + unit);
                        // 返回一个对象而不是 String，FunctionExecutor 会将其转换为 JSON
                        return new Object() {
                            public String location_name = location;
                            public int temperature = 25;
                            public String unit_name = unit;
                            public String description = "晴天";
                        };
                    })
                    .build();
            functions.add(weatherFunction);

            // 创建Tool
            List<Tool> tools = new ArrayList<>();
            tools.add(Tool.builder()
                    .functionDeclarations(functions)
                    .build());

            // 创建消息
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("user", "北京今天的天气怎么样？"));

            // 创建请求
            ChatRequest request = ChatRequest.builder()
                    .model("gemini-3-pro-preview")
                    .contents(messages)
                    .tools(tools)
                    .build();

            System.out.println(ClaudeClient.defaultObjectMapper().writeValueAsString(request));
            // 发送请求
            ChatResponse response = client.chat(request);

            System.out.println("响应候选数: " + response.getCandidates().size());
            messages.add(response.getCandidates().get(0).getContent());

            // 处理响应中的函数调用
            FunctionExecutor functionExecutor = new FunctionExecutor(functions);
            List<ChatMessageContentPart> responseParts = response.getCandidates().get(0).getContent().getParts();

            for (ChatMessageContentPart part : responseParts) {
                if (part.getFunctionCall() != null) {
                    System.out.println("收到函数调用: " + part.getFunctionCall().getName());
                    System.out.println("参数: " + part.getFunctionCall().getArgs());

                    // 如果有思想签名，打印出来
                    if (part.getThoughtSignature() != null) {
                        System.out.println("思想签名: " + part.getThoughtSignature());
                    }

                    // 执行函数并转换为消息
                    ChatMessage functionResponseMessage = functionExecutor.executeAndConvertToMessage(part.getFunctionCall());
                    System.out.println("函数响应: " + functionResponseMessage.getParts().get(0).getFunctionResponse().getResponse());
                    messages.add(functionResponseMessage);

                    request.setContents(messages);
                    ChatResponse response2 = client.chat(request);
                    System.out.println("最终响应: " + response2.getCandidates().get(0).getContent().getParts().get(0).getText());

                } else if (part.getText() != null) {
                    System.out.println("响应文本: " + part.getText());
                }
            }

        } catch (Exception e) {
            System.out.println("非流式函数调用测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testFunctionCallingStream() {
        System.out.println("========== 流式函数调用测试 ==========");
        try {
            GeminiClient client = new GeminiClient(API_KEY, Duration.ofSeconds(120), URL);

            // 定义函数
            List<ChatFunction> functions = new ArrayList<>();
            ChatFunction calculatorFunction = ChatFunction.builder()
                    .name("calculator")
                    .description("执行简单的数学运算，支持 add, subtract, multiply, divide")
                    .executor(CalculatorParams.class, params -> {
                        CalculatorParams calcParams = (CalculatorParams) params;
                        String operation = calcParams.operation;
                        Double num1 = calcParams.num1;
                        Double num2 = calcParams.num2;
                        Double result = 0.0;

                        System.out.println("执行函数: calculator");
                        System.out.println("操作: " + operation + ", 数字1: " + num1 + ", 数字2: " + num2);

                        switch (operation.toLowerCase()) {
                            case "add":
                                result = num1 + num2;
                                break;
                            case "subtract":
                                result = num1 - num2;
                                break;
                            case "multiply":
                                result = num1 * num2;
                                break;
                            case "divide":
                                result = num2 != 0 ? num1 / num2 : null;
                                break;
                        }
                        // 返回计算结果对象
                        double ans = result != null ? result : 0;
                        return new Object() {
                            public String op = operation;
                            public double n1 = num1;
                            public double n2 = num2;
                            public double res = ans;
                        };
                    })
                    .build();
            functions.add(calculatorFunction);

            // 创建Tool
            List<Tool> tools = new ArrayList<>();
            tools.add(Tool.builder()
                    .functionDeclarations(functions)
                    .build());

            // 创建消息
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("user", "请计算 25 加上 17 等于多少"));

            // 创建请求
            ChatRequest request = ChatRequest.builder()
                    .model("gemini-2.0-flash")
                    .contents(messages)
                    .tools(tools)
                    .build();

            // 流式发送请求
            System.out.println("开始流式接收响应...");
            StringBuilder fullResponse = new StringBuilder();
            List<ChatFunctionCall> functionCalls = new ArrayList<>();
            FunctionExecutor functionExecutor = new FunctionExecutor(functions);

            Flowable<StreamChatResponse> response = client.streamChat(request);
            response.doOnNext(streamResponse -> {
                if (streamResponse.getCandidates() != null && !streamResponse.getCandidates().isEmpty()) {
                    List<ChatMessageContentPart> parts = streamResponse.getCandidates().get(0).getContent().getParts();

                    for (ChatMessageContentPart part : parts) {
                        if (part.getFunctionCall() != null) {
                            ChatFunctionCall functionCall = part.getFunctionCall();
                            System.out.println("流式收到函数调用: " + functionCall.getName());
                            System.out.println("参数: " + functionCall.getArgs());

                            // 如果有思想签名，打印出来
                            if (part.getThoughtSignature() != null) {
                                System.out.println("思想签名: " + part.getThoughtSignature());
                            }

                            functionCalls.add(functionCall);
                        } else if (part.getText() != null) {
                            System.out.print(part.getText());
                            fullResponse.append(part.getText());
                        }
                    }
                }
            }).blockingSubscribe();

            System.out.println("\n流式响应完成");

            // 处理收集到的函数调用
            if (!functionCalls.isEmpty()) {
                System.out.println("\n处理函数调用...");
                for (ChatFunctionCall functionCall : functionCalls) {
                    ChatMessage functionResponseMessage = functionExecutor.executeAndConvertToMessage(functionCall);
                    System.out.println("函数响应: " + functionResponseMessage.getParts().get(0).getFunctionResponse().getResponse());
                    messages.add(functionResponseMessage);
                }

                // 可以再次发送请求以获得最终回复
                System.out.println("\n发送第二轮请求获取最终回复...");
                ChatRequest secondRequest = ChatRequest.builder()
                        .model("gemini-2.0-flash")
                        .contents(messages)
                        .tools(tools)
                        .build();

                Flowable<StreamChatResponse> secondResponse = client.streamChat(secondRequest);
                secondResponse.doOnNext(streamResponse -> {
                    if (streamResponse.getCandidates() != null && !streamResponse.getCandidates().isEmpty()) {
                        List<ChatMessageContentPart> parts = streamResponse.getCandidates().get(0).getContent().getParts();
                        for (ChatMessageContentPart part : parts) {
                            if (part.getText() != null) {
                                System.out.print(part.getText());
                            }
                        }
                    }
                }).blockingSubscribe();
            }

        } catch (Exception e) {
            System.out.println("流式函数调用测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
