package me.vacuity.ai.sdk.test;

import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.entity.ChatMessage;
import me.vacuity.ai.sdk.openai.request.ChatRequest;
import me.vacuity.ai.sdk.openai.response.ChatResponse;
import me.vacuity.ai.sdk.xai.entity.SearchParameters;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


/**
 * @description:
 * @author: vacuity
 * @create: 2025-05-22 17:04
 **/

public class XaiTest {

    public static final String API_KEY = "xai-*****";
    public static final String HOST = "https://api.x.ai/";

    @Test
    public void testSearch() {
        OpenaiClient client = new OpenaiClient(API_KEY, Duration.ofMinutes(2), HOST);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "Provide me a digest of world news in the last 24 hours."));

        SearchParameters searchParameters = SearchParameters.builder().mode("auto").build();
        ChatRequest request = ChatRequest.builder()
                .model("grok-3-latest")
                .messages(messages)
                .searchParameters(searchParameters)
                .build();
        ChatResponse response = client.chat(request);
        System.out.println(response);
    }
}
