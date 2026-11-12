package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.function.Function;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatFunction {


    @NonNull
    private String name;

    private String description;

    @JsonProperty("parameters")
    @JsonSerialize(using = ChatFunctionParametersSerializerAndDeserializer.Serializer.class)
    private Class<?> parametersClass;

    @JsonIgnore
    private Function<Object, Object> executor;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Object parametersJsonSchema;
        private me.vacuity.ai.sdk.gemini.entity.Schema response;
        private Object responseJsonSchema;
        private String behavior;
        private String name;
        private String description;
        private Class<?> parameters;
        private Function<Object, Object> executor;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public <T> Builder executor(Class<T> requestClass, Function<T, Object> executor) {
            this.parameters = requestClass;
            this.executor = (Function<Object, Object>) executor;
            return this;
        }

        /** @since 2.7.0 */
        public Builder parametersJsonSchema(Object parametersJsonSchema) {
            this.parametersJsonSchema = parametersJsonSchema;
            return this;
        }

        /** @since 2.7.0 */
        public Builder response(me.vacuity.ai.sdk.gemini.entity.Schema response) {
            this.response = response;
            return this;
        }

        /** @since 2.7.0 */
        public Builder responseJsonSchema(Object responseJsonSchema) {
            this.responseJsonSchema = responseJsonSchema;
            return this;
        }

        /** @since 2.7.0 */
        public Builder behavior(String behavior) {
            this.behavior = behavior;
            return this;
        }

        public ChatFunction build() {
            ChatFunction chatFunction = new ChatFunction();
            chatFunction.setName(name);
            chatFunction.setDescription(description);
            chatFunction.setParametersClass(parameters);
            chatFunction.setExecutor(executor);
            chatFunction.setParametersJsonSchema(parametersJsonSchema);
            chatFunction.setResponse(response);
            chatFunction.setResponseJsonSchema(responseJsonSchema);
            chatFunction.setBehavior(behavior);
            return chatFunction;
        }
    }

    /** Parameters as a plain JSON Schema, instead of {@code parameters}. Typed {@code any} in the discovery document. @since 2.7.0 */
    private Object parametersJsonSchema;
    /** Schema of the function's return value. @since 2.7.0 */
    private me.vacuity.ai.sdk.gemini.entity.Schema response;
    /** The return value as a plain JSON Schema. @since 2.7.0 */
    private Object responseJsonSchema;
    /** "BLOCKING" or "NON_BLOCKING" (asynchronous function calling). @since 2.7.0 */
    private String behavior;
}