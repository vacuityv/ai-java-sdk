package me.vacuity.ai.sdk.openai.responses.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kjetland.jackson.jsonSchema.JsonSchemaConfig;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.entity.ChatFunction;

import java.util.List;
import java.util.Map;

/**
 * A tool definition for the Responses API.
 * Supports function tools, web search, file search, code interpreter, and MCP tools.
 *
 * @author: vacuity
 * @create: 2025-01-22
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseTool {

    /**
     * The type of the tool.
     * Can be "function", "web_search_preview", "file_search", "code_interpreter", "mcp", etc.
     */
    private String type;

    // Function tool properties

    /**
     * The name of the function (for function type).
     */
    private String name;

    /**
     * A description of what the function does (for function type).
     */
    private String description;

    /**
     * The parameters the function accepts (for function type).
     * Can be a Class<?> (will be converted to JSON schema) or a Map/Object (raw JSON schema).
     */
    private Object parameters;

    /**
     * Whether to enable strict schema adherence (for function type).
     */
    private Boolean strict;

    // Web search tool properties

    /**
     * The user's location for web search.
     */
    @JsonProperty("user_location")
    private UserLocation userLocation;

    /**
     * Search context size for web search. Can be "low", "medium", or "high".
     */
    @JsonProperty("search_context_size")
    private String searchContextSize;

    // File search tool properties

    /**
     * The IDs of the vector stores to search (for file_search type).
     */
    @JsonProperty("vector_store_ids")
    private List<String> vectorStoreIds;

    /**
     * The maximum number of results to return (for file_search type).
     */
    @JsonProperty("max_num_results")
    private Integer maxNumResults;

    /**
     * Ranking options for file search results.
     */
    @JsonProperty("ranking_options")
    private RankingOptions rankingOptions;

    // Code interpreter tool properties

    /**
     * The container configuration for code interpreter.
     */
    private Object container;

    // MCP tool properties

    /**
     * The label for the MCP server.
     */
    @JsonProperty("server_label")
    private String serverLabel;

    /**
     * The URL of the MCP server.
     */
    @JsonProperty("server_url")
    private String serverUrl;

    /**
     * The connector ID for MCP.
     */
    @JsonProperty("connector_id")
    private String connectorId;

    /**
     * Allowed tools for MCP.
     */
    @JsonProperty("allowed_tools")
    private Object allowedTools;

    /**
     * HTTP headers for MCP server.
     */
    private Map<String, String> headers;

    /**
     * Whether to require approval for MCP tools.
     */
    @JsonProperty("require_approval")
    private Object requireApproval;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserLocation {
        private String type;
        private String city;
        private String country;
        private String region;
        private String timezone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankingOptions {
        private String ranker;
        @JsonProperty("score_threshold")
        private Float scoreThreshold;
    }

    // JSON Schema generator for converting Class<?> to JSON Schema
    private static final ObjectMapper SCHEMA_MAPPER = new ObjectMapper();
    private static final JsonSchemaConfig SCHEMA_CONFIG = JsonSchemaConfig.vanillaJsonSchemaDraft4();
    private static final JsonSchemaGenerator JSON_SCHEMA_GENERATOR = new JsonSchemaGenerator(SCHEMA_MAPPER, SCHEMA_CONFIG);

    /**
     * Convert a Class to JSON Schema.
     */
    private static JsonNode classToJsonSchema(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        try {
            return JSON_SCHEMA_GENERATOR.generateJsonSchema(clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JSON Schema for class: " + clazz.getName(), e);
        }
    }

    /**
     * Create a function tool from ChatFunction.
     * This allows reusing the existing ChatFunction with FunctionExecutor.
     */
    public static ResponseTool function(ChatFunction chatFunction) {
        return ResponseTool.builder()
                .type("function")
                .name(chatFunction.getName())
                .description(chatFunction.getDescription())
                .parameters(classToJsonSchema(chatFunction.getParametersClass()))
                .build();
    }

    /**
     * Create a function tool with strict mode from ChatFunction.
     */
    public static ResponseTool function(ChatFunction chatFunction, boolean strict) {
        return ResponseTool.builder()
                .type("function")
                .name(chatFunction.getName())
                .description(chatFunction.getDescription())
                .parameters(classToJsonSchema(chatFunction.getParametersClass()))
                .strict(strict)
                .build();
    }

    /**
     * Create a function tool with name, description and parameters.
     */
    public static ResponseTool function(String name, String description, Object parameters) {
        return ResponseTool.builder()
                .type("function")
                .name(name)
                .description(description)
                .parameters(parameters)
                .build();
    }

    /**
     * Create a function tool with strict mode.
     */
    public static ResponseTool function(String name, String description, Object parameters, boolean strict) {
        return ResponseTool.builder()
                .type("function")
                .name(name)
                .description(description)
                .parameters(parameters)
                .strict(strict)
                .build();
    }

    /**
     * Create a web search tool (preview version).
     */
    public static ResponseTool webSearch() {
        return ResponseTool.builder()
                .type("web_search_preview")
                .build();
    }

    /**
     * Create a web search tool with search context size.
     */
    public static ResponseTool webSearch(String searchContextSize) {
        return ResponseTool.builder()
                .type("web_search_preview")
                .searchContextSize(searchContextSize)
                .build();
    }

    /**
     * Create a web search tool with user location.
     */
    public static ResponseTool webSearch(String searchContextSize, UserLocation userLocation) {
        return ResponseTool.builder()
                .type("web_search_preview")
                .searchContextSize(searchContextSize)
                .userLocation(userLocation)
                .build();
    }

    /**
     * Create a file search tool.
     */
    public static ResponseTool fileSearch(List<String> vectorStoreIds) {
        return ResponseTool.builder()
                .type("file_search")
                .vectorStoreIds(vectorStoreIds)
                .build();
    }

    /**
     * Create a file search tool with max results.
     */
    public static ResponseTool fileSearch(List<String> vectorStoreIds, Integer maxNumResults) {
        return ResponseTool.builder()
                .type("file_search")
                .vectorStoreIds(vectorStoreIds)
                .maxNumResults(maxNumResults)
                .build();
    }

    /**
     * Create a code interpreter tool.
     */
    public static ResponseTool codeInterpreter() {
        return ResponseTool.builder()
                .type("code_interpreter")
                .container("auto")
                .build();
    }

    /**
     * Create an MCP tool with server URL.
     */
    public static ResponseTool mcp(String serverLabel, String serverUrl) {
        return ResponseTool.builder()
                .type("mcp")
                .serverLabel(serverLabel)
                .serverUrl(serverUrl)
                .build();
    }

    /**
     * Create an MCP tool with connector ID.
     */
    public static ResponseTool mcpConnector(String connectorId) {
        return ResponseTool.builder()
                .type("mcp")
                .connectorId(connectorId)
                .build();
    }
}
