package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Class<?> parametersClass;

    @JsonIgnore
    private Function<Object, Object> executor;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Boolean strict;
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
        public Builder strict(Boolean strict) {
            this.strict = strict;
            return this;
        }

        public ChatFunction build() {
            ChatFunction chatFunction = new ChatFunction();
            chatFunction.setName(name);
            chatFunction.setDescription(description);
            chatFunction.setParametersClass(parameters);
            chatFunction.setExecutor(executor);
            chatFunction.setStrict(strict);
            return chatFunction;
        }
    }

    /**
     * Structured outputs for the arguments: the model is held to the
     * parameters schema exactly.
     * <p>
     * <strong>Every field of the arguments class must be annotated
     * {@code @JsonProperty(required = true)}.</strong> Strict mode needs both
     * {@code additionalProperties: false} and every property listed in
     * {@code required}. The schema this SDK generates from the class already
     * sets the first, but lists a property as required only when it carries
     * that annotation — without it the server answers {@code 'required' is
     * required to be supplied and to be an array including every key in
     * properties}. Verified both ways against the live API.
     *
     * @since 2.7.0
     */
    @com.fasterxml.jackson.annotation.JsonProperty("strict")
    private Boolean strict;
}