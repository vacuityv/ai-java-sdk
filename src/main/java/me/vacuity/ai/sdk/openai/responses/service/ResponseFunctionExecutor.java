package me.vacuity.ai.sdk.openai.responses.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import me.vacuity.ai.sdk.openai.entity.ChatFunction;
import me.vacuity.ai.sdk.openai.responses.entity.ResponseInputItem;
import me.vacuity.ai.sdk.openai.responses.entity.ResponseOutputItem;
import me.vacuity.ai.sdk.openai.responses.entity.ResponseTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Executor for function calls in the Responses API.
 * Similar to FunctionExecutor but adapted for Responses API format.
 *
 * @author: vacuity
 * @create: 2025-01-22
 **/
public class ResponseFunctionExecutor {

    private final Map<String, ChatFunction> FUNCTIONS = new HashMap<>(16);
    private ObjectMapper MAPPER = new ObjectMapper();

    public ResponseFunctionExecutor(List<ChatFunction> functions) {
        setFunctions(functions);
    }

    public ResponseFunctionExecutor(List<ChatFunction> functions, ObjectMapper objectMapper) {
        setFunctions(functions);
        setObjectMapper(objectMapper);
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
        return ResponseInputItem.functionCallOutput(callId, "{\"error\": \"" + error + "\"}");
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
        return ResponseInputItem.functionCallOutput(
                outputItem.getCallId(),
                executeAndConvertToJson(outputItem).toString()
        );
    }

    /**
     * Execute a function call and return the result as JSON.
     */
    public JsonNode executeAndConvertToJson(ResponseOutputItem outputItem) {
        try {
            Object execution = execute(outputItem);
            if (execution instanceof TextNode) {
                JsonNode objectNode = MAPPER.readTree(((TextNode) execution).asText());
                if (objectNode.isMissingNode())
                    return (JsonNode) execution;
                return objectNode;
            }
            if (execution instanceof ObjectNode) {
                return (JsonNode) execution;
            }
            if (execution instanceof String) {
                JsonNode objectNode = MAPPER.readTree((String) execution);
                if (objectNode.isMissingNode())
                    throw new RuntimeException("Parsing exception");
                return objectNode;
            }
            return MAPPER.convertValue(execution, JsonNode.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Execute the function call and return the raw result.
     */
    @SuppressWarnings("unchecked")
    public <T> T execute(ResponseOutputItem outputItem) {
        ChatFunction function = FUNCTIONS.get(outputItem.getName());
        if (function == null) {
            throw new RuntimeException("Function not found: " + outputItem.getName());
        }
        Object obj;
        try {
            String arguments = outputItem.getArguments();
            obj = MAPPER.readValue(arguments, function.getParametersClass());
        } catch (JsonProcessingException e) {
            obj = null;
        }
        return (T) function.getExecutor().apply(obj);
    }

    /**
     * Check if a response contains function calls.
     */
    public boolean hasFunctionCalls(me.vacuity.ai.sdk.openai.responses.entity.Response response) {
        if (response == null || response.getOutput() == null) {
            return false;
        }
        return response.getOutput().stream()
                .anyMatch(item -> "function_call".equals(item.getType()));
    }

    /**
     * Get all function call output items from a response.
     */
    public List<ResponseOutputItem> getFunctionCalls(me.vacuity.ai.sdk.openai.responses.entity.Response response) {
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
    public List<ResponseInputItem> executeAllFunctionCalls(me.vacuity.ai.sdk.openai.responses.entity.Response response) {
        return getFunctionCalls(response).stream()
                .map(this::executeAndConvertToInputItemHandlingExceptions)
                .collect(Collectors.toList());
    }

    /**
     * Get all functions.
     */
    public List<ChatFunction> getFunctions() {
        return new ArrayList<>(FUNCTIONS.values());
    }

    /**
     * Get all functions as ResponseTool list (for use in requests).
     */
    public List<ResponseTool> getTools() {
        return FUNCTIONS.values().stream()
                .map(ResponseTool::function)
                .collect(Collectors.toList());
    }

    /**
     * Set functions.
     */
    public void setFunctions(List<ChatFunction> functions) {
        this.FUNCTIONS.clear();
        functions.forEach(f -> this.FUNCTIONS.put(f.getName(), f));
    }

    /**
     * Set object mapper.
     */
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.MAPPER = objectMapper;
    }
}
