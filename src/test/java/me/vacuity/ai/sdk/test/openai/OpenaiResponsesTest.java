package me.vacuity.ai.sdk.test.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Flowable;
import me.vacuity.ai.sdk.common.VacSdkException;
import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.entity.ChatFunction;
import me.vacuity.ai.sdk.openai.responses.entity.Response;
import me.vacuity.ai.sdk.openai.responses.entity.ResponseInputItem;
import me.vacuity.ai.sdk.openai.responses.entity.ResponseOutputItem;
import me.vacuity.ai.sdk.openai.responses.entity.ResponseStreamEvent;
import me.vacuity.ai.sdk.openai.responses.entity.ResponseTool;
import me.vacuity.ai.sdk.openai.responses.request.ResponseReasoning;
import me.vacuity.ai.sdk.openai.responses.request.ResponseRequest;
import me.vacuity.ai.sdk.openai.responses.service.ResponseFunctionExecutor;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static me.vacuity.ai.sdk.claude.ClaudeClient.defaultObjectMapper;
import static me.vacuity.ai.sdk.test.openai.OpenaiConstant.API_KEY;
import static me.vacuity.ai.sdk.test.openai.OpenaiConstant.BASE_URL;

/**
 * Tests for the OpenAI Responses API.
 *
 * @author: vacuity
 * @create: 2025-01-22
 **/
public class OpenaiResponsesTest {

    ObjectMapper mapper = defaultObjectMapper();

    OpenaiClient client = new OpenaiClient(API_KEY, Duration.ofSeconds(60), BASE_URL);
    
    /**
     * Simple response test with a string input.
     */
    @Test
    public void simpleResponse() {
        

        ResponseRequest request = ResponseRequest.builder()
                .model("gpt-4o")
                .input("Tell me a short joke about programming.")
                .build();

        try {
            Response response = client.createResponse(request);
            System.out.println("Response ID: " + response.getId());
            System.out.println("Status: " + response.getStatus());
            System.out.println("Output text: " + response.getOutputText());
            System.out.println("Usage: " + response.getUsage());
        } catch (VacSdkException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Test with structured input items.
     */
    @Test
    public void responseWithInputItems() {

        List<ResponseInputItem> inputItems = new ArrayList<>();
        inputItems.add(ResponseInputItem.userMessage("What is the capital of France?"));

        ResponseRequest request = ResponseRequest.builder()
                .model("gpt-4o")
                .input(inputItems)
                .instructions("You are a helpful geography assistant. Always answer in one sentence.")
                .build();

        try {
            Response response = client.createResponse(request);
            System.out.println("Response: " + response.getOutputText());
        } catch (VacSdkException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Test multi-turn conversation using previous_response_id.
     */
    @Test
    public void multiTurnConversation() {
        OpenaiClient client = new OpenaiClient(API_KEY, Duration.ofSeconds(60));

        // First turn
        ResponseRequest request1 = ResponseRequest.builder()
                .model("gpt-4o")
                .input("My name is Alice and I like programming in Java.")
                .build();

        try {
            Response response1 = client.createResponse(request1);
            System.out.println("Turn 1: " + response1.getOutputText());

            // Second turn, referencing the first response
            ResponseRequest request2 = ResponseRequest.builder()
                    .model("gpt-4o")
                    .input("What's my name and what do I like?")
                    .previousResponseId(response1.getId())
                    .build();

            Response response2 = client.createResponse(request2);
            System.out.println("Turn 2: " + response2.getOutputText());

        } catch (VacSdkException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Test streaming response.
     */
    @Test
    public void streamResponse() {
        OpenaiClient client = new OpenaiClient(API_KEY, Duration.ofSeconds(60));

        ResponseRequest request = ResponseRequest.builder()
                .model("gpt-4o")
                .input("Write a haiku about Java programming.")
                .build();

        try {
            Flowable<ResponseStreamEvent> stream = client.streamCreateResponse(request);
            StringBuilder content = new StringBuilder();

            stream.doOnNext(event -> {
                if (event != null && event.getType() != null) {
                    System.out.println("Event type: " + event.getType());

                    // Handle text delta events
                    if ("response.output_text.delta".equals(event.getType()) && event.getDelta() != null) {
                        System.out.print(event.getDelta());
                        content.append(event.getDelta());
                    }

                    // Handle completion events
                    if ("response.completed".equals(event.getType()) && event.getResponse() != null) {
                        System.out.println("\n\nCompleted. Usage: " + event.getResponse().getUsage());
                    }
                }
            }).blockingSubscribe();

            System.out.println("\n\nFull content: " + content.toString());

        } catch (VacSdkException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Test with function calling using ChatFunction and ResponseFunctionExecutor.
     * This demonstrates how to reuse the existing ChatFunction/FunctionExecutor pattern.
     */
    @Test
    public void functionCallingWithExecutor() throws JsonProcessingException {
        OpenaiClient client = new OpenaiClient(API_KEY, Duration.ofSeconds(60), BASE_URL);

        // Create function executor with ChatFunction (same as Chat Completions API)
        ResponseFunctionExecutor functionExecutor = new ResponseFunctionExecutor(Collections.singletonList(
                ChatFunction.builder()
                        .name("get_stock_value")
                        .description("Get the stock value of a stock on a date")
                        .executor(Stock.class, s -> new StockResponse(s.date, s.code, new Random().nextInt(50)))
                        .build()
        ));

        // Build request with tools from executor
        ResponseRequest request = ResponseRequest.builder()
                .model("gpt-4o")
                .input("What's the stock price of AAPL on January 15, 2023?")
                .tools(functionExecutor.getTools())
                .build();

        try {
            Response response = client.createResponse(request);
            System.out.println("Response status: " + response.getStatus());

            // Check if there are function calls
            if (functionExecutor.hasFunctionCalls(response)) {
                System.out.println("Function calls detected, executing...");

                // Execute all function calls and get input items
                List<ResponseInputItem> functionOutputs = functionExecutor.executeAllFunctionCalls(response);

                // Create follow-up request with function outputs
                List<Object> inputs = new ArrayList<>();
                inputs.addAll(functionOutputs);

                ResponseRequest followUpRequest = ResponseRequest.builder()
                        .model("gpt-4o")
                        .input(inputs)
                        .previousResponseId(response.getId())
                        .build();

                Response followUpResponse = client.createResponse(followUpRequest);
                System.out.println("Final response: " + followUpResponse.getOutputText());
            } else {
                System.out.println("Response: " + response.getOutputText());
            }

        } catch (VacSdkException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Test streaming with function calling.
     * Demonstrates how to handle function calls in streaming mode.
     * Supports multiple function calls and collects all output items for follow-up.
     */
    @Test
    public void streamFunctionCalling() {
        OpenaiClient client = new OpenaiClient(API_KEY, Duration.ofSeconds(60), BASE_URL);

        // Create function executor
        ResponseFunctionExecutor functionExecutor = new ResponseFunctionExecutor(Collections.singletonList(
                ChatFunction.builder()
                        .name("get_stock_value")
                        .description("Get the stock value of a stock on a date")
                        .executor(Stock.class, s -> new StockResponse(s.date, s.code, new Random().nextInt(50)))
                        .build()
        ));

        ResponseRequest request = ResponseRequest.builder()
                .model("gpt-4o")
                .input("What's the stock price of AAPL on January 15, 2023?")
                .tools(functionExecutor.getTools())
                .build();

        try {
            Flowable<ResponseStreamEvent> stream = client.streamCreateResponse(request);

            // Use lists to collect multiple items (supports parallel function calls)
            List<ResponseOutputItem> outputItems = new ArrayList<>();
            List<ResponseOutputItem> functionCalls = new ArrayList<>();
            Map<Integer, StringBuilder> argumentsMap = new HashMap<>();  // index -> arguments
            String[] responseId = {null};
            String[] errorMessage = {null};

            stream.doOnNext(event -> {
                if (event == null || event.getType() == null) return;

                String type = event.getType();
                System.out.println("Event: " + type);

                switch (type) {
                    case "response.created":
                        if (event.getResponse() != null) {
                            responseId[0] = event.getResponse().getId();
                            System.out.println("Response ID: " + responseId[0]);
                        }
                        break;

                    case "response.output_item.added":
                        ResponseOutputItem item = event.getItem();
                        if (item != null) {
                            System.out.println("  Item type: " + item.getType() + ", name: " + item.getName());
                            outputItems.add(item);
                            if ("function_call".equals(item.getType())) {
                                functionCalls.add(item);
                                // Initialize arguments builder for this index
                                argumentsMap.put(event.getOutputIndex(), new StringBuilder());
                            }
                        }
                        break;

                    case "response.function_call_arguments.delta":
                        if (event.getDelta() != null && event.getOutputIndex() != null) {
                            StringBuilder args = argumentsMap.get(event.getOutputIndex());
                            if (args != null) {
                                args.append(event.getDelta());
                            }
                            System.out.print(event.getDelta());
                        }
                        break;

                    case "response.function_call_arguments.done":
                        System.out.println("\nFunction arguments complete");
                        break;

                    case "response.output_item.done":
                        // Update the function call with complete arguments
                        ResponseOutputItem doneItem = event.getItem();
                        if (doneItem != null && "function_call".equals(doneItem.getType())) {
                            // Find and update the corresponding item in our list
                            for (int i = 0; i < functionCalls.size(); i++) {
                                ResponseOutputItem fc = functionCalls.get(i);
                                if (fc.getCallId() != null && fc.getCallId().equals(doneItem.getCallId())) {
                                    functionCalls.set(i, doneItem);  // Replace with complete item
                                    break;
                                }
                            }
                        }
                        break;

                    case "response.output_text.delta":
                        if (event.getDelta() != null) {
                            System.out.print(event.getDelta());
                        }
                        break;

                    case "response.completed":
                        System.out.println("\n--- Stream completed ---");
                        break;

                    case "error":
                        errorMessage[0] = event.getMessage();
                        System.out.println("Error: " + errorMessage[0]);
                        break;
                }
            }).blockingSubscribe();

            // Check for errors
            if (errorMessage[0] != null) {
                System.out.println("Stream ended with error: " + errorMessage[0]);
                return;
            }

            // If there are function calls, execute them and continue
            if (!functionCalls.isEmpty()) {
                System.out.println("\n\nExecuting " + functionCalls.size() + " function call(s)...");

                List<ResponseInputItem> functionOutputs = new ArrayList<>();
                for (ResponseOutputItem fc : functionCalls) {
                    System.out.println("  Function: " + fc.getName() + ", CallId: " + fc.getCallId());
                    System.out.println("  Arguments: " + fc.getArguments());

                    // Execute function using functionExecutor
                    ResponseInputItem output = functionExecutor.executeAndConvertToInputItem(fc);
                    System.out.println("  Result: " + output.getOutput());
                    functionOutputs.add(output);
                }

                // Continue conversation with function results
                // Note: For reasoning models, you should also pass back reasoning items
                ResponseRequest followUpRequest = ResponseRequest.builder()
                        .model("gpt-4o")
                        .input(functionOutputs)
                        .previousResponseId(responseId[0])
                        .build();

                System.out.println("\n--- Streaming follow-up response ---");
                Flowable<ResponseStreamEvent> followUpStream = client.streamCreateResponse(followUpRequest);
                followUpStream.doOnNext(event -> {
                    if (event != null && "response.output_text.delta".equals(event.getType()) && event.getDelta() != null) {
                        System.out.print(event.getDelta());
                    }
                }).blockingSubscribe();
                System.out.println();
            }

        } catch (VacSdkException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Test with function calling using manual schema definition.
     */
    @Test
    public void functionCallingManual() throws JsonProcessingException {
        OpenaiClient client = new OpenaiClient(API_KEY, Duration.ofSeconds(60), BASE_URL);

        // Define function parameters schema manually
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> cityProp = new HashMap<>();
        cityProp.put("type", "string");
        cityProp.put("description", "The city name");
        properties.put("city", cityProp);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", properties);
        parameters.put("required", Collections.singletonList("city"));

        ResponseTool weatherTool = ResponseTool.function(
                "get_weather",
                "Get the current weather for a city",
                parameters
        );

        ResponseRequest request = ResponseRequest.builder()
                .model("gpt-4o")
                .input("What's the weather like in Tokyo?")
                .tools(Collections.singletonList(weatherTool))
                .build();

        try {
            Response response = client.createResponse(request);
            System.out.println("Response status: " + response.getStatus());
            System.out.println("Output items: " + mapper.writeValueAsString(response.getOutput()));

            // Check if there's a function call in the output
            if (response.getOutput() != null) {
                for (ResponseOutputItem item : response.getOutput()) {
                    if ("function_call".equals(item.getType())) {
                        System.out.println("Function call: " + item.getName());
                        System.out.println("Call ID: " + item.getCallId());
                        System.out.println("Arguments: " + item.getArguments());

                        // Manually create function output and continue conversation
                        ResponseInputItem functionOutput = ResponseInputItem.functionCallOutput(
                                item.getCallId(),
                                "{\"temperature\": 22, \"condition\": \"sunny\", \"humidity\": 45}"
                        );

                        ResponseRequest followUpRequest = ResponseRequest.builder()
                                .model("gpt-4o")
                                .input(Collections.singletonList(functionOutput))
                                .previousResponseId(response.getId())
                                .build();

                        Response followUpResponse = client.createResponse(followUpRequest);
                        System.out.println("Final response: " + followUpResponse.getOutputText());
                    }
                }
            }

        } catch (VacSdkException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Parameter class for stock function
    public static class Stock {
        @JsonPropertyDescription("The stock code, e.g., AAPL, GOOGL")
        public String code;

        @JsonPropertyDescription("The date in format YYYYMMDD")
        @JsonProperty(required = true)
        public String date;
    }

    // Response class for stock function
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

    /**
     * Test with web search tool.
     */
    @Test
    public void webSearchTool() {

        ResponseRequest request = ResponseRequest.builder()
                .model("gpt-5")
                .input("What's the weather in Xiamen on 2026.1.26?")
                .tools(Arrays.asList(ResponseTool.webSearch()))
                .build();

        try {
            Response response = client.createResponse(request);
            System.out.println("Response: " + response.getOutputText());

            // Check for web search results
            if (response.getOutput() != null) {
                response.getOutput().forEach(item -> {
                    if ("web_search_call".equals(item.getType())) {
                        System.out.println("Web search performed");
                        if (item.getAction() != null && item.getAction().getSources() != null) {
                            item.getAction().getSources().forEach(source -> {
                                System.out.println("Source: " + source.getTitle() + " - " + source.getUrl());
                            });
                        }
                    }
                });
            }

        } catch (VacSdkException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Test streaming with web search tool.
     * Demonstrates how to handle web search results in streaming mode.
     */
    @Test
    public void streamWebSearchTool() {
        OpenaiClient client = new OpenaiClient(API_KEY, Duration.ofSeconds(120), BASE_URL);

        ResponseRequest request = ResponseRequest.builder()
                .model("gpt-4o")
                .input("What are the latest news about Java 23?")
                .tools(Arrays.asList(ResponseTool.webSearch()))
                .build();

        try {
            Flowable<ResponseStreamEvent> stream = client.streamCreateResponse(request);
            StringBuilder content = new StringBuilder();

            stream.doOnNext(event -> {
                if (event == null || event.getType() == null) return;

                String type = event.getType();
                System.out.println("Event: " + type);

                switch (type) {
                    case "response.created":
                        if (event.getResponse() != null) {
                            System.out.println("Response ID: " + event.getResponse().getId());
                        }
                        break;

                    case "response.output_item.added":
                        ResponseOutputItem item = event.getItem();
                        if (item != null) {
                            System.out.println("  Item type: " + item.getType());
                            if ("web_search_call".equals(item.getType())) {
                                System.out.println("  Web search initiated, status: " + item.getStatus());
                            }
                        }
                        break;

                    case "response.output_item.done":
                        ResponseOutputItem doneItem = event.getItem();
                        if (doneItem != null && "web_search_call".equals(doneItem.getType())) {
                            System.out.println("  Web search completed, status: " + doneItem.getStatus());
                        }
                        break;

                    case "response.output_text.delta":
                        if (event.getDelta() != null) {
                            System.out.print(event.getDelta());
                            content.append(event.getDelta());
                        }
                        break;

                    case "response.completed":
                        System.out.println("\n--- Stream completed ---");
                        if (event.getResponse() != null && event.getResponse().getOutput() != null) {
                            event.getResponse().getOutput().forEach(outputItem -> {
                                if ("web_search_call".equals(outputItem.getType())) {
                                    System.out.println("Web search performed");
                                }
                            });
                        }
                        break;
                }
            }).blockingSubscribe();

            System.out.println("\n\nFull content: " + content.toString());

        } catch (VacSdkException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Test with reasoning model.
     * To get reasoning summary, set summary to "auto" (or "detailed", "concise").
     */
    @Test
    public void reasoningModel() {

        ResponseRequest request = ResponseRequest.builder()
                .model("gpt-5.2")
                .input("Solve this step by step: What is 15% of 240, plus 25% of 180?")
                .reasoning(ResponseReasoning.builder()
                        .effort("medium")
                        .summary("auto")  // Required to get reasoning summary
                        .build())
                .build();

        try {
            Response response = client.createResponse(request);
            System.out.println("Response: " + response.getOutputText());
            System.out.println("Usage: " + response.getUsage());

            // Check for reasoning output
            if (response.getOutput() != null) {
                for (ResponseOutputItem item : response.getOutput()) {
                    System.out.println("Output item type: " + item.getType());
                    if ("reasoning".equals(item.getType())) {
                        System.out.println("Reasoning summary: " + item.getSummary());
                    }
                }
            }

        } catch (VacSdkException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Test retrieve and delete response.
     */
    @Test
    public void retrieveAndDeleteResponse() {
        OpenaiClient client = new OpenaiClient(API_KEY, Duration.ofSeconds(60));

        // First, create a response
        ResponseRequest request = ResponseRequest.builder()
                .model("gpt-4o")
                .input("Hello!")
                .store(true)  // Ensure the response is stored
                .build();

        try {
            Response response = client.createResponse(request);
            String responseId = response.getId();
            System.out.println("Created response: " + responseId);

            // Retrieve the response
            Response retrieved = client.retrieveResponse(responseId);
            System.out.println("Retrieved response status: " + retrieved.getStatus());

            // Delete the response
            Response deleted = client.deleteResponse(responseId);
            System.out.println("Deleted response: " + deleted.getId());

        } catch (VacSdkException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Test list responses.
     */
    @Test
    public void listResponses() {
        OpenaiClient client = new OpenaiClient(API_KEY, Duration.ofSeconds(60));

        try {
            List<Response> responses = client.listResponses(null, 5, "desc");
            System.out.println("Found " + responses.size() + " responses");

            responses.forEach(r -> {
                System.out.println("Response: " + r.getId() + " - " + r.getStatus() + " - " + r.getModel());
            });

        } catch (VacSdkException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
