package me.vacuity.ai.sdk.test.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Flowable;
import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.assistant.constant.AssistantStreamEventsConstant;
import me.vacuity.ai.sdk.openai.assistant.entity.Assistant;
import me.vacuity.ai.sdk.openai.assistant.entity.AssistantMessage;
import me.vacuity.ai.sdk.openai.assistant.entity.AssistantStreamResponse;
import me.vacuity.ai.sdk.openai.assistant.entity.Run;
import me.vacuity.ai.sdk.openai.assistant.entity.RunStep;
import me.vacuity.ai.sdk.openai.assistant.entity.Thread;
import me.vacuity.ai.sdk.openai.assistant.request.AssistantMessageRequest;
import me.vacuity.ai.sdk.openai.assistant.request.AssistantRequest;
import me.vacuity.ai.sdk.openai.assistant.request.RunRequest;
import me.vacuity.ai.sdk.openai.assistant.request.SubmitToolOutputsRequest;
import me.vacuity.ai.sdk.openai.assistant.request.ThreadRequest;
import me.vacuity.ai.sdk.openai.constant.ChatToolConstant;
import me.vacuity.ai.sdk.openai.entity.ChatFunction;
import me.vacuity.ai.sdk.openai.entity.ChatFunctionCall;
import me.vacuity.ai.sdk.openai.entity.ChatMessage;
import me.vacuity.ai.sdk.openai.entity.ChatTool;
import me.vacuity.ai.sdk.openai.service.FunctionExecutor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static me.vacuity.ai.sdk.claude.ClaudeClient.defaultObjectMapper;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 12:00
 **/

public class OpenaiAssistantTest {


    OpenaiClient client = new OpenaiClient(OpenaiConstant.API_KEY);

    ObjectMapper mapper = defaultObjectMapper();

    @Test
    public void createAssistant() throws JsonProcessingException {

        FunctionExecutor functionExecutor = new FunctionExecutor(Collections.singletonList(ChatFunction.builder()
                .name("get_web_content")
                .description("Get the direct content of the url address. if return '', the url is not valid.")
                .executor(WebUrl.class, w -> new WebContentRes("nothing display"))
                .build()));

        ChatTool tool1 = ChatTool.builder()
                .type(ChatToolConstant.TOOL_TYPE_FUNCTION)
                .function(functionExecutor.getFunctions().get(0))
                .build();
        ChatTool tool2 = ChatTool.builder()
                .type(ChatToolConstant.TOOL_TYPE_RETRIEVAL)
                .build();
        ChatTool tool3 = ChatTool.builder()
                .type(ChatToolConstant.TOOL_TYPE_CODE_INTERPRETER)
                .build();

        String fileId = "file-cyXRLxSuPQI65qRoEDVy3tK7";
        List<String> fileIds = Arrays.asList(fileId);
        AssistantRequest request = AssistantRequest.builder()
                .model("gpt-4-turbo-preview")
                .name("test-create")
                .description("stock assistant")
                .instructions("you are a stock assistant")
//                .fileIds(fileIds)
                .tools(Arrays.asList(tool1, tool2, tool3))
                .build();
        System.out.println(mapper.writeValueAsString(request));
        Assistant assistant = client.createAssistant(request);
        assertNotNull(assistant);
        System.out.println(assistant);
        // asst_A9CTTa6OTUOe9qpOBmZIipPS
    }

    @Test
    public void createThread() {
        ThreadRequest request = ThreadRequest.builder()
                .build();
        Thread thread = client.createThread(request);
        assertNotNull(thread);
        System.out.println(thread);
        // thread_JoX7Ou3KIRz9Nernxl5dKUWx
    }

    @Test
    public void createMessage() {
        String threadId = "thread_JoX7Ou3KIRz9Nernxl5dKUWx";
        AssistantMessageRequest request = AssistantMessageRequest.builder()
                .role("user")
                .content("introduce yourself")
                .build();
        AssistantMessage message = client.createMessage(threadId, request);
        assertNotNull(message);
        System.out.println(message);
        // thread_JoX7Ou3KIRz9Nernxl5dKUWx
    }

    @Test
    public void createRun() {
        String assistantId = "asst_A9CTTa6OTUOe9qpOBmZIipPS";
        String threadId = "thread_JoX7Ou3KIRz9Nernxl5dKUWx";
        RunRequest request = RunRequest.builder()
                .assistantId(assistantId)
                .build();
        Run run = client.createRun(threadId, request);
        assertNotNull(run);
        System.out.println(run);
        // run_R1cwe8cotNHU098VA9Ozw5At
    }

    @Test
    public void retrieveRun() {
        String threadId = "thread_JoX7Ou3KIRz9Nernxl5dKUWx";
        String runId = "run_R1cwe8cotNHU098VA9Ozw5At";
        Run run = client.retrieveRun(threadId, runId);
        assertNotNull(run);
        System.out.println(run);
        // run_R1cwe8cotNHU098VA9Ozw5At
    }

    @Test
    public void listRunSteps() {
        String threadId = "thread_JoX7Ou3KIRz9Nernxl5dKUWx";
        String runId = "run_R1cwe8cotNHU098VA9Ozw5At";
        List<RunStep> runSteps = client.listRunSteps(threadId, runId, null);
        assertNotNull(runSteps);
        System.out.println(runSteps);
    }

    @Test
    public void retrieveMessage() {
        String threadId = "thread_JoX7Ou3KIRz9Nernxl5dKUWx";
        String messageId = "msg_1gIbHuECqvAGmQbDShqX1V4a";
        AssistantMessage message = client.retrieveMessage(threadId, messageId);
        assertNotNull(message);
        System.out.println(message);
    }

    @Test
    public void streamCreateRun() {
        
        FunctionExecutor functionExecutor = new FunctionExecutor(Collections.singletonList(ChatFunction.builder()
                .name("get_web_content")
                .description("Get the direct content of the url address. if return '', the url is not valid.")
                .executor(WebUrl.class, w -> new WebContentRes("33"))
                .build()));

        ChatTool tool1 = ChatTool.builder()
                .type(ChatToolConstant.TOOL_TYPE_FUNCTION)
                .function(functionExecutor.getFunctions().get(0))
                .build();
        ChatTool tool2 = ChatTool.builder()
                .type(ChatToolConstant.TOOL_TYPE_RETRIEVAL)
                .build();
        ChatTool tool3 = ChatTool.builder()
                .type(ChatToolConstant.TOOL_TYPE_CODE_INTERPRETER)
                .build();

        String fileId = "file-cyXRLxSuPQI65qRoEDVy3tK7";
        List<String> fileIds = Arrays.asList(fileId);
        AssistantRequest request = AssistantRequest.builder()
                .model("gpt-4-turbo-preview")
                .name("AnswerBot")
                .description("answer anything")
                .instructions("answer anything")
//                .fileIds(fileIds)
                .tools(Arrays.asList(tool1, tool2, tool3))
                .build();
//        Assistant assistant = client.createAssistant(request);

        String assistantId = "asst_42vROiaI436Tx6M3GEPc0NuP";
        ThreadRequest threadRequest = ThreadRequest.builder()
                .build();
        Thread thread = client.createThread(threadRequest);

        AssistantMessageRequest messageRequest = AssistantMessageRequest.builder()
                .role("user")
                .content("what's the stock value of PAPP in 2023?")
                .build();
        AssistantMessage message = client.createMessage(thread.getId(), messageRequest);

        RunRequest runRequest = RunRequest.builder()
                .assistantId(assistantId)
                .build();

        
        AtomicReference<String> runId = new AtomicReference<>("");
        Flowable<AssistantStreamResponse> flowable = client.streamCreateRun(thread.getId(), runRequest);
        AtomicBoolean end = new AtomicBoolean(false);
        List<SubmitToolOutputsRequest.ToolOutput> toolOutputs = new ArrayList<>();
        flowable.doOnNext(response -> {
            System.out.println(response.getEvent());
            if (response.getDataClass() == Run.class) {
                runId.set(response.getRun().getId());
                Run.RequiredAction requiredAction = response.getRun().getRequiredAction();
                if (requiredAction != null) {
                    List<ChatFunctionCall> toolCalls = requiredAction.getSubmitToolOutputs().getToolCalls();
                    if (toolCalls != null) {
                        for (ChatFunctionCall toolCall : toolCalls) {
                            Optional<ChatMessage> temp = functionExecutor.executeAndConvertToMessageSafely(toolCall);
                            SubmitToolOutputsRequest.ToolOutput toolOutput = new SubmitToolOutputsRequest.ToolOutput();
                            toolOutput.setToolCallId(toolCall.getId());
                            toolOutput.setOutput(temp.get().getContent().toString());
                            toolOutputs.add(toolOutput);
                        }
                        SubmitToolOutputsRequest submitToolOutputsRequest = SubmitToolOutputsRequest.builder()
                                .toolOutputs(toolOutputs)
                                .build();
                        System.out.println("===============================");
                        System.out.println(submitToolOutputsRequest);
                        Flowable<AssistantStreamResponse> f = client.streamSubmitToolOutputs(thread.getId(), runId.get(), submitToolOutputsRequest);
                        f.doOnNext(s -> {
                            System.out.println(s);
                        }).blockingSubscribe();
                    }
                }
            }
            if (response.getEvent().equals(AssistantStreamEventsConstant.DONE)) {
                end.set(true);
            }

        }).blockingSubscribe();

        
    }


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

    public static class WebUrl {
        @JsonPropertyDescription("the url of the target web address")
        public String url;
    }

    public static class WebContentRes {

        public String content;

        public WebContentRes(String content) {
            this.content = content;
        }
    }
}
