package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One block the API dropped from the request, reported back on the response.
 * <p>
 * Only present when the "thinking-binding-controls-2026-08-01" beta flag is
 * sent; without it the drop is silent. When streaming, this arrives on the
 * message_start event.
 *
 * @author: vacuity
 * @create: 2026-09-10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InputTransformation {

    /**
     * e.g. "thinking_dropped".
     */
    private String type;

    /**
     * Where the dropped block sat, e.g. "messages.3.content.0".
     */
    private String path;

    /**
     * "model_binding_mismatch" — the current model cannot read a block an
     * newer model produced. Always dropped; prefixMismatchBehavior has no
     * effect on this case.
     * <p>
     * "prefix_binding_mismatch" — the block's prefix changed and
     * prefixMismatchBehavior was "drop_block".
     */
    private String reason;
}
