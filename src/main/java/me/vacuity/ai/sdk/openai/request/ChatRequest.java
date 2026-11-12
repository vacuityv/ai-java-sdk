package me.vacuity.ai.sdk.openai.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.entity.ChatMessage;
import me.vacuity.ai.sdk.openai.entity.ChatTool;
import me.vacuity.ai.sdk.openai.entity.PromptCacheOptions;
import me.vacuity.ai.sdk.openai.entity.StreamOptions;
import me.vacuity.ai.sdk.xai.entity.SearchParameters;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-13 14:33
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {


    private List<ChatMessage> messages;

    private String model;

    @JsonProperty("frequency_penalty")
    private Float frequencyPenalty;

    @JsonProperty("logit_bias")
    private Map<String, Integer> logitBias;

    private Boolean logprobs;

    @JsonProperty("top_logprobs")
    private Integer topLogprobs;

    /**
     * The legacy output cap. <strong>Current models reject it</strong>:
     * gpt-5.5 and gpt-5.6-terra both answer
     * {@code Unsupported parameter: 'max_tokens' is not supported with this
     * model. Use 'max_completion_tokens' instead}, and sending both is
     * rejected too. Use {@link #maxCompletionTokens} unless you are calling a
     * model old enough to still want this one.
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * The output cap current models take, counting reasoning tokens as well as
     * visible ones.
     * <p>
     * This replaces {@code max_tokens}, which those models refuse outright —
     * so before 2.6.0 the SDK could not cap output on them at all, since
     * setting the only field it had was a 400. Do not send both.
     *
     * @since 2.6.0
     */
    @JsonProperty("max_completion_tokens")
    private Integer maxCompletionTokens;

    private Integer n;

    @JsonProperty("presence_penalty")
    private Float presencePenalty;

    @JsonProperty("response_format")
    private Map<String, Object> responseFormat;

    private Integer seed;

    private Object stop;

    private Boolean stream;

    @JsonProperty("stream_options")
    private StreamOptions streamOptions;

    /**
     * Partitions the prompt cache, so one tenant's prefix never satisfies
     * another's and each is accounted for separately.
     * <p>
     * Measured on gpt-5.6-terra with one identical prompt: key "tenant-A"
     * wrote 1812 tokens then read back the same 1812, while key "tenant-B"
     * sent the very same bytes and wrote 1812 afresh with nothing cached. It
     * really does segment rather than merely label.
     *
     * @since 2.6.0
     */
    @JsonProperty("prompt_cache_key")
    private String promptCacheKey;

    /**
     * Caching mode and lifetime. See {@link PromptCacheOptions} for which of
     * the documented fields this endpoint actually accepts.
     *
     * @since 2.6.0
     */
    @JsonProperty("prompt_cache_options")
    private PromptCacheOptions promptCacheOptions;

    private Float temperature;

    @JsonProperty("top_p")
    private Double topP;

    private List<ChatTool> tools;

    @JsonProperty("tool_choice")
    private Object toolChoice;

    private String user;

    @JsonProperty("reasoning_effort")
    private String reasoningEffort;
    
    @JsonProperty("verbosity")
    private String verbosity;
    
    // XAI specific parameters
    @JsonProperty("search_parameters")
    private SearchParameters searchParameters;

    /** Audio output settings ({@code voice}, {@code format}); needs {@code modalities} to include "audio". @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("audio")
    private java.util.Map<String, Object> audio;
    /** Up to 16 key/value pairs stored with the completion. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("metadata")
    private java.util.Map<String, String> metadata;
    /** Output modalities, for example ["text", "audio"]. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("modalities")
    private java.util.List<String> modalities;
    /** Moderation settings ({@code model}, {@code policy}); passed through as given. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("moderation")
    private java.util.Map<String, Object> moderation;
    /** Whether the model may call several tools in one turn. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("parallel_tool_calls")
    private Boolean parallelToolCalls;
    /** Predicted output, for faster regeneration when most of the answer is known. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("prediction")
    private java.util.Map<String, Object> prediction;
    /** Stable per-end-user id for abuse monitoring; replaces {@code user}. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("safety_identifier")
    private String safetyIdentifier;
    /** "auto", "default", "flex", "scale", "priority" or "fast". @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("service_tier")
    private String serviceTier;
    /** Whether to store the completion for later retrieval or distillation. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("store")
    private Boolean store;
    /** Web search settings for search-capable models ({@code search_context_size}, {@code user_location}). @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("web_search_options")
    private java.util.Map<String, Object> webSearchOptions;
}
