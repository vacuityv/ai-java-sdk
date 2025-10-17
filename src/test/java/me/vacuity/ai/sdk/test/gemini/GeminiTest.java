package me.vacuity.ai.sdk.test.gemini;

import io.reactivex.Flowable;
import lombok.SneakyThrows;
import me.vacuity.ai.sdk.common.VacSdkException;
import me.vacuity.ai.sdk.gemini.GeminiClient;
import me.vacuity.ai.sdk.gemini.entity.ChatMessage;
import me.vacuity.ai.sdk.gemini.entity.ChatMessageContentPart;
import me.vacuity.ai.sdk.gemini.entity.Tool;
import me.vacuity.ai.sdk.gemini.enums.HarmBlockThreshold;
import me.vacuity.ai.sdk.gemini.enums.HarmCategory;
import me.vacuity.ai.sdk.gemini.enums.Modality;
import me.vacuity.ai.sdk.gemini.request.ChatRequest;
import me.vacuity.ai.sdk.gemini.response.ChatResponse;
import me.vacuity.ai.sdk.gemini.response.StreamChatResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
    public void debugRawStreamResponseDirect() {
        try {
            // 直接创建 OkHttpClient，不通过 Retrofit
            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build();
            
            // 手动构建请求体
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

            System.out.println(requestBody);
            
            String url = "https://generativelanguage.googleapis.com/v1alpha/models/gemini-2.5-flash-image-preview:streamGenerateContent?key=" + API_KEY;
            
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MediaType.parse("application/json"), requestBody))
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            System.out.println("=== 发送请求到: " + url + " ===");
            System.out.println("=== 请求体: ===");
            System.out.println(requestBody);
            System.out.println("=== ===");
            
            // 执行请求
            okhttp3.Response response = client.newCall(request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                System.out.println("=== 响应头信息 ===");
                System.out.println("Content-Type: " + response.header("Content-Type"));
                System.out.println("Content-Length: " + response.header("Content-Length"));
                System.out.println("Transfer-Encoding: " + response.header("Transfer-Encoding"));
                
                // 读取完整的原始响应
                InputStream inputStream = response.body().byteStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int length;
                int totalBytes = 0;
                
                System.out.println("=== 开始读取原始响应 ===");
                while ((length = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, length);
                    totalBytes += length;
                    System.out.println("读取了 " + length + " 字节，累计: " + totalBytes + " 字节");
                    
                    // 打印当前批次的原始内容（用于调试）
                    String chunk = new String(buffer, 0, length, "UTF-8");
                    System.out.println("当前批次内容:");
                    System.out.println("---START---");
                    System.out.println(chunk);
                    System.out.println("---END---");
                }
                
                String rawResponse = baos.toString("UTF-8");
                System.out.println("=== 完整原始响应 ===");
                System.out.println("总长度: " + rawResponse.length() + " 字符");
                System.out.println("完整内容:");
                System.out.println(rawResponse);
                System.out.println("=== 结束 ===");
                
                // 保存到文件方便查看
                try (FileWriter writer = new FileWriter("direct_raw_gemini_response.txt")) {
                    writer.write(rawResponse);
                    System.out.println("原始响应已保存到 direct_raw_gemini_response.txt");
                }
                
            } else {
                System.out.println("请求失败: " + response.code() + " - " + response.message());
                if (response.body() != null) {
                    System.out.println("错误内容: " + response.body().string());
                }
            }
            
            response.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}
