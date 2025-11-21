package me.vacuity.ai.sdk.gemini.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import me.vacuity.ai.sdk.gemini.GeminiClient;
import me.vacuity.ai.sdk.gemini.entity.ChatFunction;
import me.vacuity.ai.sdk.gemini.entity.ChatFunctionCall;
import me.vacuity.ai.sdk.gemini.entity.ChatMessage;
import me.vacuity.ai.sdk.gemini.entity.ChatMessageContentPart;
import me.vacuity.ai.sdk.gemini.entity.FunctionResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FunctionExecutor {

    public static final String FUNCTION_ROLE = "user";
    private final Map<String, ChatFunction> FUNCTIONS = new HashMap<>(16);
    private ObjectMapper MAPPER = GeminiClient.defaultObjectMapper();

    public FunctionExecutor(List<ChatFunction> functions) {
        setFunctions(functions);
    }

    public FunctionExecutor(List<ChatFunction> functions, ObjectMapper objectMapper) {
        setFunctions(functions);
        setObjectMapper(objectMapper);
    }

    public Optional<ChatMessage> executeAndConvertToMessageSafely(ChatFunctionCall call) {
        try {
            return Optional.ofNullable(executeAndConvertToMessage(call));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public ChatMessage convertExceptionToMessage(Exception exception, String toolName) {
        String error = exception.getMessage() == null ? exception.toString() : exception.getMessage();
        JsonNode errorNode = new TextNode(error);
        FunctionResponse functionResponse = FunctionResponse.builder()
                .name(toolName)
                .response(errorNode)
                .build();
        ChatMessageContentPart chatMessageContent = ChatMessageContentPart.builder().functionResponse(functionResponse).build();
        List<ChatMessageContentPart> parts = Collections.singletonList(chatMessageContent);
        return new ChatMessage(FUNCTION_ROLE, parts);
    }

    public ChatMessage executeAndConvertToMessage(ChatFunctionCall call) {
        FunctionResponse functionResponse = FunctionResponse.builder()
                .name(call.getName())
                .response(executeAndConvertToJson(call))
                .build();
        ChatMessageContentPart chatMessageContent = ChatMessageContentPart.builder().functionResponse(functionResponse).build();
        List<ChatMessageContentPart> parts = Collections.singletonList(chatMessageContent);
        return new ChatMessage(FUNCTION_ROLE, parts);
    }

    public ChatMessage executeAndConvertToMessage(List<ChatFunctionCall> calls) {
        List<ChatMessageContentPart> parts = new ArrayList<>(calls.size());
        for (ChatFunctionCall call : calls) {
            FunctionResponse functionResponse = FunctionResponse.builder()
                    .name(call.getName())
                    .response(executeAndConvertToJson(call))
                    .build();
            ChatMessageContentPart part = ChatMessageContentPart.builder().functionResponse(functionResponse).build();
            parts.add(part);
        }
        return new ChatMessage(FUNCTION_ROLE, parts);
    }

    public JsonNode executeAndConvertToJson(ChatFunctionCall call) {
        try {
            Object execution = execute(call);
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
            String error = e.getMessage() == null ? e.toString() : e.getMessage();
            JsonNode errorNode = new TextNode(error);
            return errorNode;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T execute(ChatFunctionCall call) {
        ChatFunction function = FUNCTIONS.get(call.getName());
        Object obj;
        try {
            JsonNode arguments = call.getArgs();
            obj = MAPPER.readValue(arguments instanceof TextNode ? arguments.asText() : arguments.toPrettyString(), function.getParametersClass());
        } catch (JsonProcessingException e) {
            obj = null;
        }
        return (T) function.getExecutor().apply(obj);
    }

    public List<ChatFunction> getFunctions() {
        return new ArrayList<>(FUNCTIONS.values());
    }

    public void setFunctions(List<ChatFunction> functions) {
        this.FUNCTIONS.clear();
        functions.forEach(f -> this.FUNCTIONS.put(f.getName(), f));
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.MAPPER = objectMapper;
    }

}
