package me.vacuity.ai.sdk.gemini.decision.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Input tokens attributed to one modality.
 *
 * @author vacuity
 * @since 2.5.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModalityTokenCount {

    /**
     * "text", "image", "audio" or "video". A PDF sent as a "document" block is
     * reported here as "image" — measured, not documented.
     */
    private String modality;

    private Integer tokens;
}
