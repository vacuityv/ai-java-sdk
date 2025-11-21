package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-12-16 15:05
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tool {

    @JsonProperty("code_execution")
    private CodeExcution codeExcution;

    @JsonProperty("functionDeclarations")
    private List<ChatFunction> functionDeclarations;

    @JsonProperty("google_search_retrieval")
    private GoogleSearchRetrieval googleSearchRetrieval;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Builder.Default
    private Map<String, Object> googleSearch = new HashMap<>();
}
