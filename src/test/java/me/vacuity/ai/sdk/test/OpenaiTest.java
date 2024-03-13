package me.vacuity.ai.sdk.test;

import io.reactivex.Flowable;
import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.entity.ChatMessage;
import me.vacuity.ai.sdk.openai.entity.ChatMessageContent;
import me.vacuity.ai.sdk.openai.entity.Model;
import me.vacuity.ai.sdk.openai.exception.VacSdkException;
import me.vacuity.ai.sdk.openai.request.ChatRequest;
import me.vacuity.ai.sdk.openai.response.ChatResponse;
import me.vacuity.ai.sdk.openai.response.StreamChatResponse;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;


/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 11:14
 **/

public class OpenaiTest {

    public static final String API_KEY = "sk-******";


    @Test
    public void chat() {

        OpenaiClient client = new OpenaiClient(API_KEY);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "introduce yourself pls"));
        ChatRequest request = ChatRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .build();
        try {
            ChatResponse response = client.chat(request);
            System.out.println(response.getSingleContent());
        } catch (VacSdkException e) {
            if (e.getDetail() != null) {
                System.out.println("err:" + e.getDetail().getError().getMessage());
            }
        }
    }

    @Test
    public void streamChat() {
//        OpenaiClient client = new OpenaiClient(API_KEY, Duration.ofSeconds(100), "https://example.com");
        OpenaiClient client = new OpenaiClient(API_KEY);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "introduce yourself pls"));
        ChatRequest request = ChatRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .build();
        Flowable<StreamChatResponse> response = client.streamChat(request);
        response.doOnNext(s -> {
            if (s != null) {
                System.out.println(s.getSingleContent());
            }

        }).blockingSubscribe();
    }

    /**
     * openai version
     *
     * @param
     * @return
     **/
    @Test
    public void vision() throws IOException {
        String imagePath = "222.jpg";
        Path path = Paths.get(imagePath);
        // read file
        byte[] imageBytes = Files.readAllBytes(path);

        InputStream is = new BufferedInputStream(new FileInputStream(imagePath));
        String mimeType = URLConnection.guessContentTypeFromStream(is);

        // convert image to base64 data
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        base64Image = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imageBytes);
        String url = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg";

        OpenaiClient client = new OpenaiClient(API_KEY, Duration.ofSeconds(120));
        List<ChatMessage> messages = new ArrayList<>();

        ChatMessageContent content = new ChatMessageContent();
        ChatMessageContent.ImageUrl imageUrl = new ChatMessageContent.ImageUrl();
        // imageUrl.setUrl(url);
        imageUrl.setUrl(base64Image);
        content.setType("image_url");
        content.setImageUrl(imageUrl);
        ChatMessageContent content2 = new ChatMessageContent();
        content2.setType("text");
        content2.setText("what is this?");

        ChatMessage chatMessage = new ChatMessage("user", Arrays.asList(content, content2));
        messages.add(chatMessage);

        ChatRequest request = ChatRequest.builder()
                .model("gpt-4-vision-preview")
                .messages(messages)
                .build();
        Flowable<StreamChatResponse> response = client.streamChat(request);
        response.doOnNext(s -> {
            System.out.println(s.getSingleContent());
        }).blockingSubscribe();
    }

    @Test
    public void listModels() {
        OpenaiClient client = new OpenaiClient(API_KEY);
        try {
            List<Model> response = client.listModels();
            System.out.println(response);
        } catch (VacSdkException e) {
            if (e.getDetail() != null) {
                System.out.println("err:" + e.getDetail().getError().getMessage());
            }
        }
    }
}
