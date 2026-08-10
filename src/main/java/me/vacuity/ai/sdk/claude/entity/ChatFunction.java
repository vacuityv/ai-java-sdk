package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;
import java.util.function.Function;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatFunction {


    private String type;
    
    @NonNull
    private String name;

    private String description;

    @JsonProperty("input_schema")
    private Class<?> parametersClass;
    
    @JsonProperty("max_uses")
    private Integer maxUses;

    /**
     * Guarantees tool_use.input validates exactly against the schema.
     * Requires additionalProperties:false and required on the schema.
     * Not compatible with programmatic tool calling.
     */
    private Boolean strict;

    /**
     * Declare the tool without loading its schema into context until the tool
     * search tool surfaces it, or a mid-conversation tool_addition adds it.
     * At least one tool must stay non-deferred.
     */
    @JsonProperty("defer_loading")
    private Boolean deferLoading;

    /**
     * Allows the tool to be invoked from inside code execution
     * (programmatic tool calling), e.g. ["code_execution_20260120"].
     */
    @JsonProperty("allowed_callers")
    private List<String> allowedCallers;

    /**
     * Cache breakpoint. Tools render first, so a breakpoint here caches the
     * tool definitions up to and including this one.
     */
    @JsonProperty("cache_control")
    private CacheControl cacheControl;

    @JsonIgnore
    private Function<Object, Object> executor;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String type;
        private String name;
        private Integer maxUses;
        private String description;
        private Class<?> parameters;
        private Function<Object, Object> executor;
        private Boolean strict;
        private Boolean deferLoading;
        private List<String> allowedCallers;
        private CacheControl cacheControl;

        public Builder strict(Boolean strict) {
            this.strict = strict;
            return this;
        }

        public Builder deferLoading(Boolean deferLoading) {
            this.deferLoading = deferLoading;
            return this;
        }

        public Builder allowedCallers(List<String> allowedCallers) {
            this.allowedCallers = allowedCallers;
            return this;
        }

        public Builder cacheControl(CacheControl cacheControl) {
            this.cacheControl = cacheControl;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }
        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder maxUses(Integer maxUses) {
            this.maxUses = maxUses;
            return this;
        }

        public <T> Builder executor(Class<T> requestClass, Function<T, Object> executor) {
            this.parameters = requestClass;
            this.executor = (Function<Object, Object>) executor;
            return this;
        }

        public ChatFunction build() {
            ChatFunction chatFunction = new ChatFunction();
            chatFunction.setType(type);
            chatFunction.setName(name);
            chatFunction.setMaxUses(maxUses);
            chatFunction.setDescription(description);
            chatFunction.setParametersClass(parameters);
            chatFunction.setExecutor(executor);
            chatFunction.setStrict(strict);
            chatFunction.setDeferLoading(deferLoading);
            chatFunction.setAllowedCallers(allowedCallers);
            chatFunction.setCacheControl(cacheControl);
            return chatFunction;
        }
    }
}