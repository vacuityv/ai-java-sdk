package me.vacuity.ai.sdk.openai.responses.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.responses.entity.ResponseTool;

import java.util.List;
import java.util.Map;

/**
 * Request body for creating a response using the Responses API.
 *
 * @author: vacuity
 * @create: 2025-01-22
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseRequest {

    /**
     * Model ID used to generate the response, like `gpt-4o` or `o3`.
     */
    private String model;

    /**
     * Text, image, or file inputs to the model, used to generate a response.
     * Can be a simple string or a list of input items.
     */
    private Object input;

    /**
     * A system (or developer) message inserted into the model's context.
     * When using along with `previous_response_id`, the instructions from a previous
     * response will not be carried over to the next response.
     */
    private String instructions;

    /**
     * The unique ID of the previous response to the model.
     * Use this to create multi-turn conversations.
     */
    @JsonProperty("previous_response_id")
    private String previousResponseId;

    /**
     * An upper bound for the number of tokens that can be generated for a response,
     * including visible output tokens and reasoning tokens.
     */
    @JsonProperty("max_output_tokens")
    private Integer maxOutputTokens;

    /**
     * Set of 16 key-value pairs that can be attached to an object.
     */
    private Map<String, String> metadata;

    /**
     * What sampling temperature to use, between 0 and 2.
     */
    private Float temperature;

    /**
     * An alternative to sampling with temperature, called nucleus sampling.
     */
    @JsonProperty("top_p")
    private Float topP;

    /**
     * An array of tools the model may call while generating a response.
     */
    private List<ResponseTool> tools;

    /**
     * How the model should select which tool (or tools) to use when generating a response.
     */
    @JsonProperty("tool_choice")
    private Object toolChoice;

    /**
     * Whether to allow the model to run tool calls in parallel.
     */
    @JsonProperty("parallel_tool_calls")
    private Boolean parallelToolCalls;

    /**
     * The truncation strategy to use for the model response.
     * Can be "auto" or "disabled".
     */
    private String truncation;

    /**
     * A stable identifier for your end-users.
     */
    private String user;

    /**
     * Whether to store the generated model response for later retrieval via API.
     */
    private Boolean store;

    /**
     * If set to true, the model response data will be streamed to the client.
     */
    private Boolean stream;

    /**
     * Whether to run the model response in the background.
     */
    private Boolean background;

    /**
     * Specify additional output data to include in the model response.
     */
    private List<String> include;

    /**
     * Configuration options for a text response from the model.
     */
    private ResponseTextConfig text;

    /**
     * Configuration options for reasoning models.
     */
    private ResponseReasoning reasoning;

    /**
     * Specifies the processing type used for serving the request.
     */
    @JsonProperty("service_tier")
    private String serviceTier;

    /**
     * The maximum number of total calls to built-in tools that can be processed in a response.
     */
    @JsonProperty("max_tool_calls")
    private Integer maxToolCalls;

    /**
     * An integer between 0 and 20 specifying the number of most likely tokens to return.
     */
    @JsonProperty("top_logprobs")
    private Integer topLogprobs;
}
