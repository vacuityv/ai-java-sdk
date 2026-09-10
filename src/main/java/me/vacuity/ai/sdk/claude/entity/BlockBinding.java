package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Controls what happens when a thinking block's prefix no longer matches.
 * <p>
 * A thinking block stays valid only while the top-level system prompt, the
 * tools, and every message before it are unchanged. If any of them changed,
 * the block is invalid and this setting decides the outcome.
 * <p>
 * Requires the "thinking-binding-controls-2026-08-01" beta flag — send it via
 * {@code ChatRequest#betas}.
 * <p>
 * The API enforces the prefix check by default for accounts created on or
 * after 2026-08-31. Older accounts are only checked when this field is set,
 * but later models will enforce it for every account.
 *
 * @author: vacuity
 * @create: 2026-09-10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlockBinding {

    /**
     * "error" (the default) — reject the request with a 400 naming the first
     * failing block. "drop_block" — drop each failing block and every thinking
     * block after it, and let the request succeed.
     */
    @JsonProperty("prefix_mismatch_behavior")
    private String prefixMismatchBehavior;

    /**
     * Fail loudly on a prefix mismatch. This is the API default.
     */
    public static BlockBinding error() {
        return BlockBinding.builder().prefixMismatchBehavior("error").build();
    }

    /**
     * Drop the invalid thinking blocks and continue. The dropped blocks are
     * not billed, the model answers without their reasoning, and the prompt
     * cache restarts at the edit.
     */
    public static BlockBinding dropBlock() {
        return BlockBinding.builder().prefixMismatchBehavior("drop_block").build();
    }
}
