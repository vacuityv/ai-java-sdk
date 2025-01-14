package me.vacuity.ai.sdk.openai.realtime.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-01-14 14:11
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnDetection {
    
    private String type;

    private Float threshold;

    @JsonProperty("prefix_padding_ms")
    private Integer prefixPaddingMs;
    
    @JsonProperty("silence_duration_ms")
    private Integer silenceDurationMs;
}
