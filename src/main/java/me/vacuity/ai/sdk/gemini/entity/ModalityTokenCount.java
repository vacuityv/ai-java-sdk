package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token count for a single modality, as used by the usageMetadata breakdowns.
 *
 * @author: vacuity
 * @create: 2026-10-21
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModalityTokenCount {

    /**
     * e.g. "TEXT", "IMAGE", "AUDIO".
     */
    private String modality;

    private Integer tokenCount;
}
