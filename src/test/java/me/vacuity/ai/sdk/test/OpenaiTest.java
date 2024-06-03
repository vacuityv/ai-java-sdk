package me.vacuity.ai.sdk.test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import io.reactivex.Flowable;
import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.entity.ChatFunction;
import me.vacuity.ai.sdk.openai.entity.ChatFunctionCall;
import me.vacuity.ai.sdk.openai.entity.ChatMessage;
import me.vacuity.ai.sdk.openai.entity.ChatMessageContent;
import me.vacuity.ai.sdk.openai.entity.ChatTool;
import me.vacuity.ai.sdk.openai.entity.Model;
import me.vacuity.ai.sdk.openai.entity.StreamOptions;
import me.vacuity.ai.sdk.openai.exception.VacSdkException;
import me.vacuity.ai.sdk.openai.request.ChatRequest;
import me.vacuity.ai.sdk.openai.response.ChatResponse;
import me.vacuity.ai.sdk.openai.response.StreamChatResponse;
import me.vacuity.ai.sdk.openai.service.FunctionExecutor;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static me.vacuity.ai.sdk.claude.ClaudeClient.defaultObjectMapper;
import static me.vacuity.ai.sdk.test.openai.OpenaiConstant.API_KEY;


/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 11:14
 **/

public class OpenaiTest {


    ObjectMapper mapper = defaultObjectMapper();

    public static class Stock {
        @JsonPropertyDescription("the code of the stock, for example: AAPL, GOOGL")
        public String code;

        @JsonPropertyDescription("the date of the stock, for example: 20240101")
        @JsonProperty(required = true)
        public String date;
    }

    public static class StockResponse {
        public String date;

        public String code;

        public Integer value;

        public StockResponse(String date, String code, Integer value) {
            this.date = date;
            this.code = code;
            this.value = value;
        }
    }


    @Test
    public void chat() throws JsonProcessingException {

        FunctionExecutor functionExecutor = new FunctionExecutor(Collections.singletonList(ChatFunction.builder()
                .name("get_stock_value")
                .description("get the stock value of a stock on a date")
                .executor(Stock.class, w -> new StockResponse(w.date, w.code, new Random().nextInt(50)))
                .build()));

        ChatTool tool = ChatTool.builder()
                .type("function")
                .function(functionExecutor.getFunctions().get(0))
                .build();

        OpenaiClient client = new OpenaiClient(API_KEY);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "what's the stock of AAPL on October 8, 2023"));
        ChatRequest request = ChatRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .tools(Arrays.asList(tool))
                .build();
        System.out.println(mapper.writeValueAsString(request));
        try {
            ChatResponse response = client.chat(request);
            List<ChatFunctionCall> functionCalls = response.getChoices().get(0).getMessage().getToolCalls();
            if (functionCalls != null) {
                ChatMessage assistantMsg = ChatMessage.builder()
                        .role("assistant")
                        .toolCalls(functionCalls)
                        .build();
                messages.add(assistantMsg);

                for (ChatFunctionCall functionCall : functionCalls) {
                    System.out.println("Trying to execute " + functionCall.getFunction().getName() + "...");
                    Optional<ChatMessage> message = functionExecutor.executeAndConvertToMessageSafely(functionCall);
                    if (message.isPresent()) {
                        System.out.println("Executed " + functionCall.getFunction().getName() + ".");
                        messages.add(message.get());
                        request.setMessages(messages);
                        response = client.chat(request);
                    } else {
                        System.out.println("Something went wrong with the execution of " + functionCall.getFunction().getName() + "...");
                        return;
                    }
                }

            }
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
        FunctionExecutor functionExecutor = new FunctionExecutor(Collections.singletonList(ChatFunction.builder()
                .name("get_stock_value")
                .description("get the stock value of a stock on a date")
                .executor(Stock.class, w -> new StockResponse(w.date, w.code, new Random().nextInt(50)))
                .build()));

        ChatTool tool = ChatTool.builder()
                .type("function")
                .function(functionExecutor.getFunctions().get(0))
                .build();


        OpenaiClient client = new OpenaiClient(API_KEY);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "你好"));
        ChatRequest request = ChatRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .tools(Arrays.asList(tool))
                .build();
        Flowable<StreamChatResponse> response = null;
        StringBuilder content = new StringBuilder();
        while (StringUtils.isBlank(content.toString())) {
            response = client.streamChat(request);
            List<ChatFunctionCall> functionCalls = new ArrayList<>();
            response.doOnNext(s -> {
                if (s != null) {
                    if (s.getChoices().get(0).getDelta().getToolCalls() != null) {
                        for (ChatFunctionCall functionCall : s.getChoices().get(0).getDelta().getToolCalls()) {
                            int index = functionCall.getIndex();
                            if (index <= functionCalls.size() - 1) {
                                ChatFunctionCall init = functionCalls.get(index);
                                ChatFunctionCall.ChatFunctionDetail detail = init.getFunction();
                                String argumentsPart = detail.getArguments() == null ? "" : detail.getArguments().asText();
                                String addPart = functionCall.getFunction().getArguments() == null ? "" : functionCall.getFunction().getArguments().asText();
                                detail.setArguments(new TextNode(argumentsPart + addPart));
                                init.setFunction(detail);
                            } else {
                                ChatFunctionCall init = new ChatFunctionCall();
                                init.setId(functionCall.getId());
                                init.setType(functionCall.getType());
                                ChatFunctionCall.ChatFunctionDetail detail = new ChatFunctionCall.ChatFunctionDetail();
                                detail.setName(functionCall.getFunction().getName());
                                detail.setArguments(functionCall.getFunction().getArguments());
                                init.setFunction(detail);
                                functionCalls.add(init);
                            }
                        }
                    } else {
                        System.out.println(s.getSingleContent());
                        content.append(s.getSingleContent());
                    }
                }
            }).blockingSubscribe();
            if (functionCalls.size() > 0) {
                ChatMessage assistantMsg = ChatMessage.builder()
                        .role("assistant")
                        .toolCalls(functionCalls)
                        .build();
                messages.add(assistantMsg);
                for (ChatFunctionCall functionCall : functionCalls) {
                    System.out.println("Trying to execute " + functionCall.getFunction().getName() + "...");
                    Optional<ChatMessage> message = functionExecutor.executeAndConvertToMessageSafely(functionCall);
                    if (message.isPresent()) {
                        System.out.println("Executed " + functionCall.getFunction().getName() + ".");
                        messages.add(message.get());
                        request.setMessages(messages);
                    } else {
                        System.out.println("Something went wrong with the execution of " + functionCall.getFunction().getName() + "...");
                    }
                }
            }
        }

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

    @Test
    public void stream() {

        OpenaiClient client = new OpenaiClient(API_KEY);

        
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "hello"));

        StreamOptions options = StreamOptions.builder()
                .includeUsage(true)
                .build();
        ChatRequest request = ChatRequest.builder()
                .model("gpt-4o")
                .messages(messages)
                .temperature(1f)
                .presencePenalty(0.1f)
                .streamOptions(options)
                .build();
        try {
            Flowable<StreamChatResponse> response = client.streamChat(request);
            response.doOnNext(s -> {
                if (s != null) {
                    if (s.getUsage() != null) {
                        System.out.println("============");
                        System.out.println(s.getUsage());
                    }
                    System.out.print(s.getSingleContent());
                }

            }).blockingSubscribe();
        } catch (VacSdkException e) {
            if (e.getDetail() != null) {
                System.out.println("err:" + e.getDetail().getError().getMessage());
            }
        }

    }
}
