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

    /**
     * Enables the built-in Google Search tool. The API expects an empty object,
     * so set it to an empty map — {@link #googleSearch()} builds one for you.
     * <p>
     * Left null it is omitted, which is what you want on requests that should
     * not search.
     * <p>
     * Note: before 1.13.0 this field defaulted to an empty map and was
     * annotated NON_EMPTY, so it was dropped from the body whatever you set —
     * the built-in search could not actually be enabled through this SDK.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> googleSearch;

    /**
     * A tool entry that enables the built-in Google Search.
     */
    public static Tool googleSearch() {
        return Tool.builder().googleSearch(new HashMap<>()).build();
    }

    /**
     * A single tool entry carrying both function declarations and the built-in
     * Google Search. On gemini-4-argon this combination also requires
     * {@code toolConfig.includeServerSideToolInvocations} on the request.
     */
    public static Tool functionsWithGoogleSearch(List<ChatFunction> functions) {
        return Tool.builder()
                .functionDeclarations(functions)
                .googleSearch(new HashMap<>())
                .build();
    }
}
