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
     * Can be "function", "web_search", "file_search", "code_interpreter", "mcp", etc.
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
                .type("web_search")
                .build();
    }

    /**
     * Create a web search tool with search context size.
     */
    public static ResponseTool webSearch(String searchContextSize) {
        return ResponseTool.builder()
                .type("web_search")
                .searchContextSize(searchContextSize)
                .build();
    }

    /**
     * Create a web search tool with user location.
     */
    public static ResponseTool webSearch(String searchContextSize, UserLocation userLocation) {
        return ResponseTool.builder()
                .type("web_search")
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
    
    // the follow methods only works in grok

    // Grok web_search tool properties

    /**
     * Restrict web searches to specified domains only (max 5).
     * Only works with Grok's web_search tool.
     */
    @JsonProperty("allowed_domains")
    private List<String> allowedDomains;

    /**
     * Prevent web searches on specified domains (max 5).
     * Only works with Grok's web_search tool.
     */
    @JsonProperty("excluded_domains")
    private List<String> excludedDomains;

    /**
     * Enable image understanding for web_search or x_search.
     * Grants access to view_image tool for analyzing images.
     * Only works with Grok.
     */
    @JsonProperty("enable_image_understanding")
    private Boolean enableImageUnderstanding;

    // Grok x_search tool properties

    /**
     * Consider posts only from specified X handles (max 10).
     * Only works with Grok's x_search tool.
     */
    @JsonProperty("allowed_x_handles")
    private List<String> allowedXHandles;

    /**
     * Exclude posts from specified X handles (max 10).
     * Only works with Grok's x_search tool.
     */
    @JsonProperty("excluded_x_handles")
    private List<String> excludedXHandles;

    /**
     * Start date for X search results (ISO8601 format).
     * Only works with Grok's x_search tool.
     */
    @JsonProperty("from_date")
    private String fromDate;

    /**
     * End date for X search results (ISO8601 format).
     * Only works with Grok's x_search tool.
     */
    @JsonProperty("to_date")
    private String toDate;

    /**
     * Enable video understanding for x_search.
     * Grants access to view_x_video tool for analyzing video content.
     * Only works with Grok's x_search tool.
     */
    @JsonProperty("enable_video_understanding")
    private Boolean enableVideoUnderstanding;

    /**
     * Create a Grok web search tool.
     * Allows the agent to search the web and browse pages.
     * Only works with Grok.
     */
    public static ResponseTool grokWebSearch() {
        return ResponseTool.builder()
                .type("web_search")
                .build();
    }

    /**
     * Create a Grok web search tool with domain restrictions.
     * Only works with Grok.
     *
     * @param allowedDomains restrict searches to these domains only (max 5)
     * @param excludedDomains prevent searches on these domains (max 5)
     */
    public static ResponseTool grokWebSearch(List<String> allowedDomains, List<String> excludedDomains) {
        return ResponseTool.builder()
                .type("web_search")
                .allowedDomains(allowedDomains)
                .excludedDomains(excludedDomains)
                .build();
    }

    /**
     * Create a Grok web search tool with full configuration.
     * Only works with Grok.
     *
     * @param allowedDomains restrict searches to these domains only (max 5)
     * @param excludedDomains prevent searches on these domains (max 5)
     * @param enableImageUnderstanding enable image analysis during search
     */
    public static ResponseTool grokWebSearch(List<String> allowedDomains, List<String> excludedDomains,
                                              boolean enableImageUnderstanding) {
        return ResponseTool.builder()
                .type("web_search")
                .allowedDomains(allowedDomains)
                .excludedDomains(excludedDomains)
                .enableImageUnderstanding(enableImageUnderstanding)
                .build();
    }

    /**
     * Create a Grok X search tool.
     * Allows the agent to perform keyword search, semantic search, user search, and thread fetch on X.
     * Only works with Grok.
     */
    public static ResponseTool grokXSearch() {
        return ResponseTool.builder()
                .type("x_search")
                .build();
    }

    /**
     * Create a Grok X search tool with handle restrictions.
     * Only works with Grok.
     *
     * @param allowedXHandles consider posts only from these X handles (max 10)
     * @param excludedXHandles exclude posts from these X handles (max 10)
     */
    public static ResponseTool grokXSearch(List<String> allowedXHandles, List<String> excludedXHandles) {
        return ResponseTool.builder()
                .type("x_search")
                .allowedXHandles(allowedXHandles)
                .excludedXHandles(excludedXHandles)
                .build();
    }

    /**
     * Create a Grok X search tool with date range.
     * Only works with Grok.
     *
     * @param fromDate start date for search results (ISO8601 format)
     * @param toDate end date for search results (ISO8601 format)
     */
    public static ResponseTool grokXSearchWithDateRange(String fromDate, String toDate) {
        return ResponseTool.builder()
                .type("x_search")
                .fromDate(fromDate)
                .toDate(toDate)
                .build();
    }

    /**
     * Create a Grok X search tool with full configuration.
     * Only works with Grok.
     *
     * @param allowedXHandles consider posts only from these X handles (max 10)
     * @param excludedXHandles exclude posts from these X handles (max 10)
     * @param fromDate start date for search results (ISO8601 format)
     * @param toDate end date for search results (ISO8601 format)
     * @param enableImageUnderstanding enable image analysis
     * @param enableVideoUnderstanding enable video analysis
     */
    public static ResponseTool grokXSearch(List<String> allowedXHandles, List<String> excludedXHandles,
                                            String fromDate, String toDate,
                                            Boolean enableImageUnderstanding, Boolean enableVideoUnderstanding) {
        return ResponseTool.builder()
                .type("x_search")
                .allowedXHandles(allowedXHandles)
                .excludedXHandles(excludedXHandles)
                .fromDate(fromDate)
                .toDate(toDate)
                .enableImageUnderstanding(enableImageUnderstanding)
                .enableVideoUnderstanding(enableVideoUnderstanding)
                .build();
    }

    /**
     * Create a Grok code execution tool.
     * The model can write and execute Python code for calculations, data analysis, and complex computations.
     * Only works with Grok.
     */
    public static ResponseTool grokCodeExecution() {
        return ResponseTool.builder()
                .type("code_execution")
                .build();
    }

    /**
     * Create a Grok collections search tool.
     * The model can search through uploaded knowledge bases and collections to retrieve relevant information.
     * Only works with Grok.
     */
    public static ResponseTool grokCollectionsSearch() {
        return ResponseTool.builder()
                .type("collections_search")
                .build();
    }

    /**
     * Create a Grok attachment search tool.
     * Allows intelligent document search through uploaded files.
     * Only works with Grok.
     */
    public static ResponseTool grokAttachmentSearch() {
        return ResponseTool.builder()
                .type("attachment_search")
                .build();
    }
}
