package me.vacuity.ai.sdk.openai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import me.vacuity.ai.sdk.openai.entity.ChatFunction;
import me.vacuity.ai.sdk.openai.entity.ChatFunctionCall;
import me.vacuity.ai.sdk.openai.entity.ChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FunctionExecutor {

    public static final String FUNCTION_ROLE = "tool";
    private final Map<String, ChatFunction> FUNCTIONS = new HashMap<>();
    private ObjectMapper MAPPER = new ObjectMapper();

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

    public ChatMessage executeAndConvertToMessageHandlingExceptions(ChatFunctionCall call) {
        try {
            return executeAndConvertToMessage(call);
        } catch (Exception exception) {
            exception.printStackTrace();
            return convertExceptionToMessage(exception, call.getId());
        }
    }

    public ChatMessage convertExceptionToMessage(Exception exception, String toolCallId) {
        String error = exception.getMessage() == null ? exception.toString() : exception.getMessage();
        return new ChatMessage(FUNCTION_ROLE, "{\"error\": \"" + error + "\"}", toolCallId);
    }

    public ChatMessage executeAndConvertToMessage(ChatFunctionCall call) {
        return new ChatMessage(FUNCTION_ROLE, executeAndConvertToJson(call).toPrettyString(), call.getId());
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
            return MAPPER.readValue(MAPPER.writeValueAsString(execution), JsonNode.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T execute(ChatFunctionCall call) {
        ChatFunction function = FUNCTIONS.get(call.getFunction().getName());
        Object obj;
        try {
            JsonNode arguments = call.getFunction().getArguments();
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
