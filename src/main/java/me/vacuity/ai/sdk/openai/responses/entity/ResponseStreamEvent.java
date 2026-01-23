package me.vacuity.ai.sdk.openai.responses.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A streaming event from the Responses API.
 *
 * @author: vacuity
 * @create: 2025-01-22
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseStreamEvent {

    /**
     * The type of the streaming event.
     * Possible values include:
     * - response.created
     * - response.in_progress
     * - response.completed
     * - response.failed
     * - response.incomplete
     * - response.output_item.added
     * - response.output_item.done
     * - response.content_part.added
     * - response.content_part.done
     * - response.output_text.delta
     * - response.output_text.done
     * - response.refusal.delta
     * - response.refusal.done
     * - response.function_call_arguments.delta
     * - response.function_call_arguments.done
     * - response.file_search_call.in_progress
     * - response.file_search_call.searching
     * - response.file_search_call.completed
     * - response.web_search_call.in_progress
     * - response.web_search_call.searching
     * - response.web_search_call.completed
     * - response.code_interpreter_call.in_progress
     * - response.code_interpreter_call.interpreting
     * - response.code_interpreter_call.completed
     * - response.mcp_call.in_progress
     * - response.mcp_call.completed
     * - response.mcp_call.failed
     * - response.reasoning_summary_part.added
     * - response.reasoning_summary_part.done
     * - response.reasoning_summary_text.delta
     * - response.reasoning_summary_text.done
     * - error
     */
    private String type;

    /**
     * The response object (for response.* events).
     */
    private Response response;

    /**
     * The output item (for response.output_item.* events).
     * Note: API may use either "item" or "output_item" field.
     */
    @JsonProperty("output_item")
    private ResponseOutputItem outputItem;

    /**
     * The item (alternative field name for output_item).
     */
    private ResponseOutputItem item;

    /**
     * The index of the output item.
     */
    @JsonProperty("output_index")
    private Integer outputIndex;

    /**
     * The content part (for response.content_part.* events).
     */
    @JsonProperty("content_part")
    private ResponseOutputContent contentPart;

    /**
     * The index of the content part.
     */
    @JsonProperty("content_index")
    private Integer contentIndex;

    /**
     * The text delta (for response.output_text.delta events).
     */
    private String delta;

    /**
     * The full text (for response.output_text.done events).
     */
    private String text;

    /**
     * The item ID.
     */
    @JsonProperty("item_id")
    private String itemId;

    /**
     * The error message (for error events).
     */
    private String message;

    /**
     * The error code (for error events).
     */
    private String code;

    /**
     * The sequence number of the event.
     */
    @JsonProperty("sequence_number")
    private Integer sequenceNumber;

    /**
     * Part information for content parts.
     */
    private Part part;

    /**
     * Summary part for reasoning events.
     */
    @JsonProperty("summary_part")
    private SummaryPart summaryPart;

    /**
     * Get the output item from either "item" or "output_item" field.
     * API typically uses "item" field.
     */
    public ResponseOutputItem getOutputItemOrItem() {
        return item != null ? item : outputItem;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        private String type;
        private String text;
        private String refusal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryPart {
        private String type;
        private String text;
    }
}
