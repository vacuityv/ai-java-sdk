package me.vacuity.ai.sdk.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.reactivex.Flowable;
import me.vacuity.ai.sdk.claude.ClaudeClient;
import me.vacuity.ai.sdk.claude.constant.ResponseTypeConstant;
import me.vacuity.ai.sdk.claude.entity.ChatFunction;
import me.vacuity.ai.sdk.claude.entity.ChatMessage;
import me.vacuity.ai.sdk.claude.entity.ChatMessageContent;
import me.vacuity.ai.sdk.claude.exception.VacSdkException;
import me.vacuity.ai.sdk.claude.request.ChatRequest;
import me.vacuity.ai.sdk.claude.response.ChatResponse;
import me.vacuity.ai.sdk.claude.response.StreamChatResponse;
import me.vacuity.ai.sdk.claude.service.FunctionExecutor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static me.vacuity.ai.sdk.claude.ClaudeClient.defaultObjectMapper;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 11:14
 **/

public class ClaudeTest {

    public static final String API_KEY = "sk-*****";


    @Test
    public void chat() {
        ClaudeClient client = new ClaudeClient(API_KEY);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "introduce yourself pls"));
        ChatRequest request = ChatRequest.builder()
                .model("claude-3-opus-20240229")
                .messages(messages)
                .maxTokens(1024)
                .build();
        try {
            ChatResponse response = client.chat(request);
            System.out.println(response);
        } catch (VacSdkException e) {
            if (e.getDetail() != null) {
                System.out.println(e.getDetail().getError().getMessage());
            }
        }
    }

    @Test
    public void streamChat() {
//        ClaudeClient client = new ClaudeClient(API_KEY, Duration.ofSeconds(100), "https://example.com");
        ClaudeClient client = new ClaudeClient(API_KEY);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "鲁迅为什么打周树人"));
        ChatRequest request = ChatRequest.builder()
                .model("claude-3-opus-20240229")
                .messages(messages)
                .maxTokens(1024)
                .build();
        Flowable<StreamChatResponse> response = client.streamChat(request);
        response.doOnNext(s -> {
            if (ResponseTypeConstant.CONTENT_BLOCK_DELTA.equals(s.getType())) {
                ChatMessageContent content = s.getDelta();
                System.out.print(content.getText());
            } else if (ResponseTypeConstant.ERROR.equals(s.getType())) {
                System.out.println(s.getError().getMessage());
            }
            
            // get input token
            if (s.getMessage() != null) {
                System.out.println(s.getMessage().getUsage());
            }

            // get output token
            if (s.getUsage() != null) {
                System.out.println(s.getType());
                System.out.println(s.getUsage());
            }
            
        }).blockingSubscribe();
    }

    @Test
    public void proxyChat() {
        String host = "127.0.0.1";
        int port = 7890;
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));

        ClaudeClient client = new ClaudeClient(API_KEY, Duration.ofSeconds(60), proxy);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "introduce yourself pls"));
        ChatRequest request = ChatRequest.builder()
                .model("claude-3-opus-20240229")
                .messages(messages)
                .maxTokens(1024)
                .build();
        try {
            ChatResponse response = client.chat(request);
            System.out.println(response.getContent().get(0).getText());
        } catch (VacSdkException e) {
            if (e.getDetail() != null) {
                System.out.println(e.getDetail().getError().getMessage());
            }
        }
    }

    /**
     * claude version
     *
     * @param
     * @return
     **/
    @Test
    public void vision() throws IOException {
        String imagePath = "sonatype.jpg";
        // 读取图片文件
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));

        // 将图片文件转换为Base64编码
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        ClaudeClient client = new ClaudeClient(API_KEY, Duration.ofSeconds(120));
        List<ChatMessage> messages = new ArrayList<>();

        ChatMessageContent content = new ChatMessageContent();
        ChatMessageContent.ContentSource source = new ChatMessageContent.ContentSource();
        source.setType("base64");
        source.setMediaType("image/jpeg");
        source.setData(base64Image);

        content.setType("image");
        content.setSource(source);
        ChatMessageContent content2 = new ChatMessageContent();
        content2.setType("text");
        content2.setText("what is this?");


        ChatMessage chatMessage = new ChatMessage("user", Arrays.asList(content, content2));
        messages.add(chatMessage);

        ChatRequest request = ChatRequest.builder()
                .model("claude-3-opus-20240229")
                .messages(messages)
                .maxTokens(1024)
                .build();
        try {
            ChatResponse response = client.chat(request);
            System.out.println(response);
        } catch (VacSdkException e) {
            if (e.getDetail() != null) {
                System.out.println(e.getDetail().getError().getMessage());
            }
        }
    }

    @Test
    public void chatWithFunction() {

        FunctionExecutor functionExecutor = new FunctionExecutor(Collections.singletonList(ChatFunction.builder()
                .name("get_stock_value")
                .description("get the stock value of a stock on a date")
                .executor(OpenaiTest.Stock.class, w -> new OpenaiTest.StockResponse(w.date, w.code, new Random().nextInt(50)))
                .build()));
        ClaudeClient client = new ClaudeClient(API_KEY, Duration.ofSeconds(120));
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "what's the stock value of APPL on 2023-02-18"));
        ChatRequest request = ChatRequest.builder()
                .model("claude-3-opus-20240229")
                .messages(messages)
                .tools(functionExecutor.getFunctions())
                .maxTokens(1024)
                .build();
        try {
            ChatResponse response = client.chat(request);
            ChatMessage respMsg = new ChatMessage("assistant", response.getContent());
            messages.add(respMsg);
            List<ChatMessageContent> contents = response.getContent();
            for (ChatMessageContent content : contents) {
                if ("tool_use".equals(content.getType())) {
                    System.out.println(defaultObjectMapper().writeValueAsString(content));
                    ChatMessage functionMessage = functionExecutor.executeAndConvertToMessage(content);
                    messages.add(functionMessage);
                    request.setMessages(messages);
                    ChatResponse response2 = client.chat(request);
                    System.out.println("function response:");
                    System.out.println(defaultObjectMapper().writeValueAsString(response2.getContent()));
                } else {
                    System.out.println(content.getText());
                }
            }
        } catch (VacSdkException e) {
            if (e.getDetail() != null) {
                System.out.println(e.getDetail().getError().getMessage());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
