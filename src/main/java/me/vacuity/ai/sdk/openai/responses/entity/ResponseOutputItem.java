package me.vacuity.ai.sdk.openai.responses.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * An output item from the Responses API.
 * Can be a message, function call, web search, file search, reasoning item, etc.
 *
 * @author: vacuity
 * @create: 2025-01-22
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseOutputItem {

    /**
     * The unique ID of the output item.
     */
    private String id;

    /**
     * The type of the output item.
     * Can be "message", "function_call", "web_search_call", "file_search_call",
     * "code_interpreter_call", "reasoning", "mcp_call", etc.
     */
    private String type;

    /**
     * The role of the message (for message type). Always "assistant".
     */
    private String role;

    /**
     * The content of the output message (for message type).
     */
    private List<ResponseOutputContent> content;

    /**
     * The status of the item.
     * One of "in_progress", "completed", or "incomplete".
     */
    private String status;

    // Function call properties

    /**
     * The unique ID of the function tool call (for function_call type).
     */
    @JsonProperty("call_id")
    private String callId;

    /**
     * The name of the function being called (for function_call type).
     */
    private String name;

    /**
     * The arguments passed to the function (for function_call type).
     */
    private String arguments;

    // Web search properties

    /**
     * The search action details (for web_search_call type).
     */
    private WebSearchAction action;

    // File search properties

    /**
     * The search queries (for file_search_call type).
     */
    private List<String> queries;

    /**
     * The search results (for file_search_call type).
     */
    private List<FileSearchResult> results;

    // Code interpreter properties

    /**
     * The code being executed (for code_interpreter_call type).
     */
    private String code;

    /**
     * The outputs from code execution (for code_interpreter_call type).
     */
    private List<CodeInterpreterOutput> outputs;

    // Reasoning properties

    /**
     * The reasoning summary (for reasoning type).
     */
    private List<ReasoningSummary> summary;

    // MCP call properties

    /**
     * The MCP server label (for mcp_call type).
     */
    @JsonProperty("server_label")
    private String serverLabel;

    /**
     * The MCP tool output (for mcp_call type).
     */
    private String output;

    /**
     * The MCP tool error (for mcp_call type).
     */
    private String error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebSearchAction {
        private String type;
        private String query;
        private List<WebSearchSource> sources;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebSearchSource {
        private String url;
        private String title;
        private String snippet;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileSearchResult {
        @JsonProperty("file_id")
        private String fileId;
        private String filename;
        private Float score;
        private String text;
        private Map<String, Object> attributes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CodeInterpreterOutput {
        private String type;
        private String text;
        @JsonProperty("file_id")
        private String fileId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReasoningSummary {
        private String type;
        private String text;
    }
}
