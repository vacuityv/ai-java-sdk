package me.vacuity.ai.sdk.openai.responses.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for context compaction.
 *
 * @author: vacuity
 * @create: 2026-08-10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContextManagement {

    /**
     * The context management entry type. Currently only "compaction" is supported.
     */
    private String type;

    /**
     * Token threshold at which compaction should be triggered for this entry.
     */
    @JsonProperty("compact_threshold")
    private Integer compactThreshold;
}
