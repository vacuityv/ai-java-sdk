package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-06-03 16:43
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseStartMessage {

    private String id;

    private Usage usage;

    /**
     * Blocks the API dropped from the request. When streaming, this arrives on
     * the message_start event. Requires the
     * "thinking-binding-controls-2026-08-01" beta flag.
     */
    @JsonProperty("input_transformations")
    private List<InputTransformation> inputTransformations;
}
