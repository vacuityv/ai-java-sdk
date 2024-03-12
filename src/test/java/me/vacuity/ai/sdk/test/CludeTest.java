package me.vacuity.ai.sdk.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Flowable;
import me.vacuity.ai.sdk.claude.api.ClaudeApi;
import me.vacuity.ai.sdk.claude.entity.ChatMessage;
import me.vacuity.ai.sdk.claude.entity.ChatMessageContent;
import me.vacuity.ai.sdk.claude.exception.VacSdkException;
import me.vacuity.ai.sdk.claude.request.ChatRequest;
import me.vacuity.ai.sdk.claude.response.ChatResponse;
import me.vacuity.ai.sdk.claude.response.StreamChatResponse;
import me.vacuity.ai.sdk.claude.ClaudeClient;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import retrofit2.Retrofit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static me.vacuity.ai.sdk.claude.ClaudeClient.defaultClient;
import static me.vacuity.ai.sdk.claude.ClaudeClient.defaultObjectMapper;
import static me.vacuity.ai.sdk.claude.ClaudeClient.defaultRetrofit;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 11:14
 **/

public class CludeTest {

    public static final String API_KEY = "sk-******";


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
        ClaudeClient client = new ClaudeClient(API_KEY, Duration.ofSeconds(100), "https://example.com");
//        ClaudeClient client = new ClaudeClient(API_KEY);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "鲁迅为什么打周树人"));
        ChatRequest request = ChatRequest.builder()
                .model("claude-3-opus-20240229")
                .messages(messages)
                .maxTokens(1024)
                .build();
        Flowable<StreamChatResponse> response = client.streamChat(request);
        response.doOnNext(s -> {
            if ("content_block_delta".equals(s.getType())) {
                ChatMessageContent content = s.getDelta();
                System.out.print(content.getText());
            } else if ("error".equals(s.getType())) {
                System.out.println(s.getError().getMessage());
            }
        }).blockingSubscribe();
    }

    @Test
    public void proxyChat() {
        String host = "127.0.0.1";
        int port = 7890;
        ObjectMapper mapper = defaultObjectMapper();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        OkHttpClient httpClient = defaultClient(API_KEY, Duration.ofSeconds(60))
                .newBuilder()
                .proxy(proxy)
                .build();
        Retrofit retrofit = defaultRetrofit(httpClient, mapper, null);
        ClaudeApi api = retrofit.create(ClaudeApi.class);
        ClaudeClient client = new ClaudeClient(api);
        
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
}
