package me.vacuity.ai.sdk.openai.responses.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import me.vacuity.ai.sdk.openai.entity.ChatFunction;
import me.vacuity.ai.sdk.openai.entity.ChatFunctionCall;
import me.vacuity.ai.sdk.openai.responses.entity.Response;
import me.vacuity.ai.sdk.openai.responses.entity.ResponseInputItem;
import me.vacuity.ai.sdk.openai.responses.entity.ResponseOutputItem;
import me.vacuity.ai.sdk.openai.responses.entity.ResponseTool;
import me.vacuity.ai.sdk.openai.service.FunctionExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Executor for function calls in the Responses API.
 * Uses composition to reuse FunctionExecutor's core logic.
 *
 * @author: vacuity
 * @create: 2025-01-22
 **/
public class ResponseFunctionExecutor {

    private final FunctionExecutor executor;
    private ObjectMapper mapper;

    public ResponseFunctionExecutor(List<ChatFunction> functions) {
        this.executor = new FunctionExecutor(functions);
        this.mapper = new ObjectMapper();
    }

    public ResponseFunctionExecutor(List<ChatFunction> functions, ObjectMapper objectMapper) {
        this.executor = new FunctionExecutor(functions, objectMapper);
        this.mapper = objectMapper;
    }

    /**
     * Execute a function call from the response output and convert to input item safely.
     *
     * @param outputItem The output item containing the function call
     * @return Optional containing the function call output input item, or empty if execution failed
     */
    public Optional<ResponseInputItem> executeAndConvertToInputItemSafely(ResponseOutputItem outputItem) {
        try {
            return Optional.ofNullable(executeAndConvertToInputItem(outputItem));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    /**
     * Execute a function call and convert to input item, handling exceptions.
     *
     * @param outputItem The output item containing the function call
     * @return The function call output input item
     */
    public ResponseInputItem executeAndConvertToInputItemHandlingExceptions(ResponseOutputItem outputItem) {
        try {
            return executeAndConvertToInputItem(outputItem);
        } catch (Exception exception) {
            exception.printStackTrace();
            return convertExceptionToInputItem(exception, outputItem.getCallId());
        }
    }

    /**
     * Convert an exception to a function call output input item.
     */
    public ResponseInputItem convertExceptionToInputItem(Exception exception, String callId) {
        String error = exception.getMessage() == null ? exception.toString() : exception.getMessage();
        ObjectNode errorNode = mapper.createObjectNode();
        errorNode.put("error", error);
        return ResponseInputItem.functionCallOutput(callId, errorNode.toString());
    }

    /**
     * Execute a function call from the response output and convert to input item.
     *
     * @param outputItem The output item containing the function call
     * @return The function call output input item
     */
    public ResponseInputItem executeAndConvertToInputItem(ResponseOutputItem outputItem) {
        if (!"function_call".equals(outputItem.getType())) {
            throw new IllegalArgumentException("Output item is not a function call");
        }

        ChatFunctionCall call = convertToFunctionCall(outputItem);
        JsonNode result = executor.executeAndConvertToJson(call);
        return ResponseInputItem.functionCallOutput(outputItem.getCallId(), result.toString());
    }

    /**
     * Convert ResponseOutputItem to ChatFunctionCall for reusing FunctionExecutor logic.
     */
    private ChatFunctionCall convertToFunctionCall(ResponseOutputItem outputItem) {
        ChatFunctionCall call = new ChatFunctionCall();
        call.setId(outputItem.getCallId());
        call.setType("function");

        ChatFunctionCall.ChatFunctionDetail detail = new ChatFunctionCall.ChatFunctionDetail();
        detail.setName(outputItem.getName());
        detail.setArguments(new TextNode(outputItem.getArguments()));
        call.setFunction(detail);

        return call;
    }

    /**
     * Check if a response contains function calls.
     */
    public boolean hasFunctionCalls(Response response) {
        if (response == null || response.getOutput() == null) {
            return false;
        }
        return response.getOutput().stream()
                .anyMatch(item -> "function_call".equals(item.getType()));
    }

    /**
     * Get all function call output items from a response.
     */
    public List<ResponseOutputItem> getFunctionCalls(Response response) {
        if (response == null || response.getOutput() == null) {
            return new ArrayList<>();
        }
        return response.getOutput().stream()
                .filter(item -> "function_call".equals(item.getType()))
                .collect(Collectors.toList());
    }

    /**
     * Execute all function calls in a response and return the input items.
     */
    public List<ResponseInputItem> executeAllFunctionCalls(Response response) {
        return getFunctionCalls(response).stream()
                .map(this::executeAndConvertToInputItemHandlingExceptions)
                .collect(Collectors.toList());
    }

    /**
     * Get all functions.
     */
    public List<ChatFunction> getFunctions() {
        return executor.getFunctions();
    }

    /**
     * Get all functions as ResponseTool list (for use in requests).
     */
    public List<ResponseTool> getTools() {
        return executor.getFunctions().stream()
                .map(ResponseTool::function)
                .collect(Collectors.toList());
    }

    /**
     * Set functions.
     */
    public void setFunctions(List<ChatFunction> functions) {
        executor.setFunctions(functions);
    }

    /**
     * Set object mapper.
     */
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.mapper = objectMapper;
        executor.setObjectMapper(objectMapper);
    }
}
