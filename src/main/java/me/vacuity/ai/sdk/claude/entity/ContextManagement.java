package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Context editing — clears stale tool results or thinking blocks from the
 * transcript before the model sees it. This is not compaction (which
 * summarizes); cleared content is removed.
 * <p>
 * Beta — send the "context-management-2025-06-27" flag via ChatRequest#betas.
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

    private List<Edit> edits;

    public static ContextManagement of(Edit... edits) {
        return ContextManagement.builder().edits(java.util.Arrays.asList(edits)).build();
    }

    public static ContextManagement clearToolUses() {
        return ContextManagement.builder()
                .edits(Collections.singletonList(Edit.clearToolUses()))
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Edit {

        /**
         * Strategy: "clear_tool_uses_20250919" or "clear_thinking_20251015".
         */
        private String type;

        /**
         * For clear_tool_uses: also clear the tool_use input parameters,
         * not just the results.
         */
        @JsonProperty("clear_tool_inputs")
        private Boolean clearToolInputs;

        public static Edit clearToolUses() {
            return Edit.builder().type("clear_tool_uses_20250919").build();
        }

        public static Edit clearThinking() {
            return Edit.builder().type("clear_thinking_20251015").build();
        }
    }
}
