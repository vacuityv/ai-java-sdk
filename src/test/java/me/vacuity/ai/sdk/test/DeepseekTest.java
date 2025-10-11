package me.vacuity.ai.sdk.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.reactivex.Flowable;
import me.vacuity.ai.sdk.common.VacSdkException;
import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.entity.ChatMessage;
import me.vacuity.ai.sdk.openai.entity.StreamOptions;
import me.vacuity.ai.sdk.openai.request.ChatRequest;
import me.vacuity.ai.sdk.openai.response.ChatResponse;
import me.vacuity.ai.sdk.openai.response.ChatResponseMessage;
import me.vacuity.ai.sdk.openai.response.StreamChatResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-30 22:24
 **/


public class DeepseekTest {

    public static final String URL = "https://api.deepseek.com/";

    public static final String API_KEY = System.getenv("DEEPSEEK_API_KEY");

    public static final OpenaiClient client = new OpenaiClient(API_KEY, Duration.ofMinutes(1), URL);

    @Test
    public void chat() {


        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "what can u do"));
        ChatRequest request = ChatRequest.builder()
//                .model("deepseek-chat")
                .model("deepseek-reasoner")
                .messages(messages)
                .build();
        try {
            ChatResponse response = client.chat(request);
            System.out.println(response.getChoices().get(0).getMessage().getReasoningContent());
            System.out.println("==================");
            System.out.println(response.getSingleContent());
            System.out.println("==================");
            System.out.println(response.getUsage());
        } catch (VacSdkException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void streamChat() throws JsonProcessingException {

        OpenaiClient client = new OpenaiClient(API_KEY, Duration.ofMinutes(1), URL);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "what can you do"));
        StreamOptions streamOptions = StreamOptions.builder()
                .includeUsage(true)
                .build();
        ChatRequest request = ChatRequest.builder()
//                .model("deepseek-chat")
                .model("deepseek-reasoner")
                .messages(messages)
                .streamOptions(streamOptions)
                .build();

        StringBuilder stringBuilder = new StringBuilder();
        Flowable<StreamChatResponse> response = client.streamChat(request);
        response.doOnNext(s -> {
            //
            if (s != null && s.getChoices().size() > 0) {

                ChatResponseMessage message = s.getChoices().get(0).getDelta();
                String res = message.getReasoningContent();
                if (res != null) {
                    System.out.println("A:" + res);
                }

                String txt = s.getSingleContent();
                if (txt != null) {
                    stringBuilder.append(txt);
                    System.out.println("B:" + txt);
                }


            }
            if (s != null && s.getUsage() != null) {
                System.out.println(s.getUsage());
            }
        }).blockingSubscribe();
        System.out.println(stringBuilder);
    }
}
