package me.vacuity.ai.sdk.claude.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.claude.entity.ChatMessageContent;
import me.vacuity.ai.sdk.claude.entity.InputTransformation;
import me.vacuity.ai.sdk.claude.entity.StopDetails;
import me.vacuity.ai.sdk.claude.entity.Usage;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:17
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {

    private String id;

    private String type;

    private String role;

    private List<ChatMessageContent> content;

    private String model;

    @JsonProperty("stop_reason")
    private String stopReason;

    @JsonProperty("stop_sequence")
    private String stopSequence;

    /**
     * Populated only when stopReason is "refusal" — carries the policy
     * category that declined the request. Null for every other stop reason,
     * so always check stopReason before reading it.
     */
    @JsonProperty("stop_details")
    private StopDetails stopDetails;

    /**
     * Blocks the API dropped from the request. Only populated when the
     * "thinking-binding-controls-2026-08-01" beta flag is sent; without it
     * drops are silent.
     */
    @JsonProperty("input_transformations")
    private List<InputTransformation> inputTransformations;

    private Usage usage;
}
