package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Extra detail about why generation stopped. Populated only when
 * stop_reason is "refusal"; null for every other stop reason.
 *
 * @author: vacuity
 * @create: 2026-08-10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StopDetails {

    /**
     * Always "refusal" when present.
     */
    private String type;

    /**
     * The policy category that declined the request, e.g. "cyber", "bio",
     * "reasoning_extraction". An open set — may also be null.
     */
    private String category;

    /**
     * Human-readable explanation. Not guaranteed to be present.
     */
    private String explanation;
}
