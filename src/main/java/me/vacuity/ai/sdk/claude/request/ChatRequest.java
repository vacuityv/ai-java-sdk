package me.vacuity.ai.sdk.claude.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import me.vacuity.ai.sdk.claude.entity.CacheControl;
import me.vacuity.ai.sdk.claude.entity.ChatFunction;
import me.vacuity.ai.sdk.claude.entity.ChatMessage;
import me.vacuity.ai.sdk.claude.entity.ChatMessageContent;
import me.vacuity.ai.sdk.claude.entity.ContextManagement;
import me.vacuity.ai.sdk.claude.entity.McpServer;
import me.vacuity.ai.sdk.claude.entity.OutputConfig;
import me.vacuity.ai.sdk.claude.entity.Thinking;
import me.vacuity.ai.sdk.claude.entity.ToolChoice;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:12
 **/


@Data
@Builder
public class ChatRequest {

    private String model;

    private List<ChatMessage> messages;

    /**
     * System prompt. Either a String, or a List of text
     * {@link ChatMessageContent} blocks when you need to attach a
     * cache_control breakpoint.
     */
    private Object system;

    @NonNull
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    private Map<String, Object> metadata;

    @JsonProperty("stop_sequences")
    private List<String> stopSequences;

    private Boolean stream;

    /**
     * @deprecated Removed on Opus 4.7 and later, Opus 5, Sonnet 5 and Fable 5 —
     * sending it returns a 400. Steer behaviour with prompting instead.
     */
    @Deprecated
    private Float temperature;

    /**
     * @deprecated See {@link #temperature}.
     */
    @Deprecated
    @JsonProperty("top_p")
    private Float topP;

    /**
     * @deprecated See {@link #temperature}.
     */
    @Deprecated
    @JsonProperty("top_k")
    private Integer topK;

    private List<ChatFunction> tools;

    /**
     * How the model should use the provided tools — force a specific tool,
     * force any tool, or disable tool use for this request.
     */
    @JsonProperty("tool_choice")
    private ToolChoice toolChoice;

    private Thinking thinking;

    @JsonProperty("output_config")
    private OutputConfig outputConfig;

    /**
     * Applies a cache breakpoint to the last cacheable block in the request —
     * the simplest option when you don't need fine-grained placement.
     */
    @JsonProperty("cache_control")
    private CacheControl cacheControl;

    /**
     * "auto" or "standard_only" — whether to use priority capacity.
     */
    @JsonProperty("service_tier")
    private String serviceTier;

    /**
     * "standard" or "fast". Fast mode is a research preview on Opus 5 and
     * Opus 4.8, first-party API only — send the "fast-mode-2026-02-01" beta flag.
     */
    private String speed;

    /**
     * Geographic region for inference processing.
     */
    @JsonProperty("inference_geo")
    private String inferenceGeo;

    /**
     * Container id to reuse, or an object carrying the skills to load.
     * Beta — send "code-execution-2025-08-25" and "skills-2025-10-02".
     */
    private Object container;

    /**
     * Remote MCP servers. Each one must also be referenced by an
     * {@code mcp_toolset} entry in {@link #tools}.
     * Beta — send "mcp-client-2025-11-20".
     */
    @JsonProperty("mcp_servers")
    private List<McpServer> mcpServers;

    /**
     * Clears stale tool results or thinking blocks from the transcript.
     * Beta — send "context-management-2025-06-27".
     */
    @JsonProperty("context_management")
    private ContextManagement contextManagement;

    /**
     * Server-side retry on substitute models when the requested model declines
     * for policy reasons. Either the string "default" (with beta flag
     * "server-side-fallback-2026-07-01") or a list of {model} entries (with
     * "server-side-fallback-2026-06-01").
     */
    private Object fallbacks;

    /**
     * The fallback_credit_token from a prior refusal's stop_details, replayed
     * so the retry bills previously-cached spans at cache-read rates.
     * Beta — send "fallback-credit-2026-06-01".
     */
    @JsonProperty("fallback_credit_token")
    private String fallbackCreditToken;

    /**
     * Request-level cache diagnostics.
     * Beta — send "cache-diagnosis-2026-04-07".
     */
    private Object diagnostics;

    /**
     * Values for the anthropic-beta header, sent as a comma-separated list.
     * Not part of the request body.
     */
    @JsonIgnore
    private List<String> betas;

    public static class ChatRequestBuilder {

        /**
         * Plain-text system prompt.
         */
        public ChatRequestBuilder system(String system) {
            this.system = system;
            return this;
        }

        /**
         * System prompt as text blocks — required if you want to attach a
         * cache_control breakpoint to it.
         */
        public ChatRequestBuilder system(List<ChatMessageContent> system) {
            this.system = system;
            return this;
        }
    }
}
