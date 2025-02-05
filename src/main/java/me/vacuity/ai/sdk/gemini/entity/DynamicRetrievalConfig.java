package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-02-05 12:35
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicRetrievalConfig {

    @Builder.Default
    private String mode = "MODE_DYNAMIC";

    @Builder.Default
    @JsonProperty("dynamic_threshold")
    private Float dynamicThreshold = 0.5F;
}
