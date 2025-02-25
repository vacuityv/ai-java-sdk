package me.vacuity.ai.sdk.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.reactivex.Flowable;
import me.vacuity.ai.sdk.claude.ClaudeClient;
import me.vacuity.ai.sdk.claude.constant.ResponseTypeConstant;
import me.vacuity.ai.sdk.claude.entity.ChatFunction;
import me.vacuity.ai.sdk.claude.entity.ChatFunctionCall;
import me.vacuity.ai.sdk.claude.entity.ChatMessage;
import me.vacuity.ai.sdk.claude.entity.ChatMessageContent;
import me.vacuity.ai.sdk.claude.entity.Thinking;
import me.vacuity.ai.sdk.claude.request.ChatRequest;
import me.vacuity.ai.sdk.claude.response.ChatResponse;
import me.vacuity.ai.sdk.claude.response.StreamChatResponse;
import me.vacuity.ai.sdk.claude.service.FunctionExecutor;
import me.vacuity.ai.sdk.common.VacSdkException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static me.vacuity.ai.sdk.claude.ClaudeClient.defaultObjectMapper;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 11:14
 **/

public class ClaudeTest {

    public static final String API_KEY = "sk-*****";
    public static final String MODEL = "claude-3-7-sonnet-20250219";

    ClaudeClient client = new ClaudeClient(API_KEY);

    @Test
    public void chat() {

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "introduce yourself pls"));
        Thinking thinking = Thinking.builder()
                .type("enabled")
                .budgetTokens(32000)
                .build();
        ChatRequest request = ChatRequest.builder()
                .model(MODEL)
                .messages(messages)
                .thinking(thinking)
                .maxTokens(128000)
                .temperature(1f)
                .build();
        try {
            ChatResponse response = client.chat(request);
            System.out.println(response);
        } catch (VacSdkException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void streamChat() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "鲁迅为什么打周树人"));
        Thinking thinking = Thinking.builder()
                .type("enabled")
                .budgetTokens(32000)
                .build();
        ChatRequest request = ChatRequest.builder()
                .model(MODEL)
                .messages(messages)
                .thinking(thinking)
                .maxTokens(128000)
                .temperature(1f)
                .build();
        Flowable<StreamChatResponse> response = client.streamChat(request);
        response.doOnNext(s -> {
            if (ResponseTypeConstant.CONTENT_BLOCK_DELTA.equals(s.getType())) {
                ChatMessageContent content = s.getDelta();
                if (ResponseTypeConstant.DELTA_TYPE_TEXT.equals(content.getType())) {
                    System.out.print("text:\n" + content.getText());
                } else if (ResponseTypeConstant.DELTA_TYPE_THINKING.equals(content.getType())) {
                    System.out.print("thinking:\n" + content.getThinking());
                }
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
                .model("claude-3-5-sonnet-20240620")
                .messages(messages)
                .maxTokens(1024)
                .build();
        try {
            ChatResponse response = client.chat(request);
            System.out.println(response.getContent().get(0).getText());
        } catch (VacSdkException e) {
            System.out.println(e.getMessage());
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
                .model("claude-3-5-sonnet-20240620")
                .messages(messages)
                .maxTokens(1024)
                .build();
        try {
            ChatResponse response = client.chat(request);
            System.out.println(response);
        } catch (VacSdkException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void chatWithFunction() {

        FunctionExecutor functionExecutor = new FunctionExecutor(Collections.singletonList(ChatFunction.builder()
                .name("get_stock_value")
                .description("get the stock value of a stock on a date")
                .executor(OpenaiTest.Stock.class, w -> new OpenaiTest.StockResponse(w.date, w.code, new Random().nextInt(50)))
                .build()));
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "what's the stock value of AAPL on 2023-02-15"));

        Thinking thinking = Thinking.builder()
                .type("enabled")
                .budgetTokens(32000)
                .build();
        ChatRequest request = ChatRequest.builder()
                .model(MODEL)
                .messages(messages)
                .tools(functionExecutor.getFunctions())
                .thinking(thinking)
                .maxTokens(128000)
                .temperature(1f)
                .build();
        try {
            ChatResponse response = client.chat(request);
            ChatMessage respMsg = new ChatMessage("assistant", response.getContent());
            messages.add(respMsg);
            List<ChatMessageContent> contents = response.getContent();
            for (ChatMessageContent content : contents) {
                if ("tool_use".equals(content.getType())) {
                    System.out.println(defaultObjectMapper().writeValueAsString(content));
                    ChatFunctionCall call = new ChatFunctionCall();
                    call = new ChatFunctionCall();
                    call.setId(content.getId());
                    call.setName(content.getName());
                    call.setArguments(content.getInput());
                    ChatMessage functionMessage = functionExecutor.executeAndConvertToMessage(call);
                    messages.add(functionMessage);
                    request.setMessages(messages);
                    ChatResponse response2 = client.chat(request);
                    System.out.println("function response:");
                    System.out.println(defaultObjectMapper().writeValueAsString(response2.getContent()));
                } else {
                    System.out.println(content.getThinking());
                    System.out.println("===============");
                    System.out.println(content.getText());
                }
            }
        } catch (VacSdkException e) {
            System.out.println(e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void streamChatWithFunction() throws JsonProcessingException {
        FunctionExecutor functionExecutor = new FunctionExecutor(Collections.singletonList(ChatFunction.builder()
                .name("get_stock_value")
                .description("get the stock value of a stock on a date")
                .executor(OpenaiTest.Stock.class, w -> new OpenaiTest.StockResponse(w.date, w.code, new Random().nextInt(50)))
                .build()));

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "what's the stock value of AAPL on 20230218"));
        Thinking thinking = Thinking.builder()
                .type("enabled")
                .budgetTokens(32000)
                .build();
        ChatRequest request = ChatRequest.builder()
                .model(MODEL)
                .messages(messages)
                .tools(functionExecutor.getFunctions())
                .thinking(thinking)
                .maxTokens(128000)
                .temperature(1f)
                .build();

        StringBuilder sb = new StringBuilder();
        StringBuilder sbk = new StringBuilder();
        StringBuilder sbkg = new StringBuilder();

        ChatMessage assistantMsg = ChatMessage.builder()
                .role("assistant")
                .build();

        List<ChatMessageContent> assistantContents = new ArrayList<>();

        while (true) {
            Flowable<StreamChatResponse> response = client.streamChat(request);


            Map<Integer, ChatFunctionCall> functionMap = new HashMap<>();
            Map<Integer, String> functionArguMap = new HashMap<>();
            response.doOnNext(s -> {
                System.out.println(s);
                if (ResponseTypeConstant.CONTENT_BLOCK_START.equalsIgnoreCase(s.getType()) || ResponseTypeConstant.CONTENT_BLOCK_DELTA.equalsIgnoreCase(s.getType())) {
                    ChatMessageContent content = s.getDelta() == null ? s.getContentBlock() : s.getDelta();
                    if (ResponseTypeConstant.DELTA_TYPE_TEXT.equals(content.getType())) {
                        sb.append(content.getText());
                    } else if (ResponseTypeConstant.DELTA_TYPE_THINKING.equals(content.getType())) {
                        sbk.append(content.getThinking());
                    } else if (ResponseTypeConstant.DELTA_TYPE_SIGNATURE.equals(content.getType())) {
                        sbkg.append(content.getSignature());
                    } else if (ResponseTypeConstant.DELTA_TYPE_TOOL_USE.equals(content.getType())) {
                        ChatFunctionCall call = new ChatFunctionCall();
                        call = new ChatFunctionCall();
                        call.setIndex(s.getIndex());
                        call.setId(content.getId());
                        call.setName(content.getName());
                        functionMap.put(s.getIndex(), call);
                    } else if (ResponseTypeConstant.DELTA_TYPE_JSON.equals(content.getType())) {
                        String argu = functionArguMap.get(s.getIndex());
                        if (argu == null) {
                            argu = "";
                        }
                        argu += content.getPartialJson();
                        functionArguMap.put(s.getIndex(), argu);
                    }
                }

            }).blockingSubscribe();

            System.out.println(sbk.toString());
            System.out.println(sb.toString());
            ChatMessageContent content0 = ChatMessageContent.builder()
                    .type("thinking")
                    .thinking(sbk.toString())
                    .signature(sbkg.toString())
                    .build();

            ChatMessageContent content1 = ChatMessageContent.builder()
                    .type("text")
                    .text(sb.toString())
                    .build();
            assistantContents.add(content0);
            assistantContents.add(content1);

            boolean functionFlag = false;
            List<ChatFunctionCall> calls = new ArrayList<>();
            if (functionMap.size() > 0) {
                functionFlag = true;
                for (Map.Entry<Integer, ChatFunctionCall> entry : functionMap.entrySet()) {
                    ChatFunctionCall call = entry.getValue();
                    String argu = functionArguMap.get(entry.getKey());
                    call.setArguments(defaultObjectMapper().readTree(argu));
                    calls.add(call);

                    ChatMessageContent toolContent = ChatMessageContent.builder()
                            .type("tool_use")
                            .id(call.getId())
                            .name(call.getName())
                            .input(call.getArguments())
                            .build();
                    assistantContents.add(toolContent);
                }
            }
            if (functionFlag) {
                assistantMsg.setContent(assistantContents);
                messages.add(assistantMsg);
                messages.add(functionExecutor.executeAndConvertToMessage(calls));

                System.out.println(defaultObjectMapper().writeValueAsString(messages));
            } else {
                break;
            }
        }
    }
}
