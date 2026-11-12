package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The {@code output_config} carried by a mid-conversation system message.
 * <p>
 * Deliberately not {@link OutputConfig}, which is the top-level type: only
 * {@code effort} is accepted in this position, and Anthropic's own SDKs model
 * the two separately for the same reason. Reusing the top-level type here
 * would let a caller set {@code format} or {@code taskBudget} on a message and
 * get a rejection with nothing in the SDK to explain it.
 *
 * @author vacuity
 * @since 2.4.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemMessageOutputConfig {

    /**
     * The effort level to apply from this point on: "low", "medium", "high",
     * "xhigh" or "max".
     * <p>
     * Not validated here — the server answers
     * {@code Input should be 'low', 'medium', 'high', 'xhigh' or 'max'} with
     * the message index, which is clearer than anything the SDK could invent,
     * and a client-side copy of the enum would outlive the next model that
     * adds a level. Note that "adaptive" is <em>not</em> one of them: it is a
     * thinking type, and sending it here is a 400.
     */
    private String effort;

    /**
     * Shorthand for {@code SystemMessageOutputConfig.builder().effort(e).build()}.
     */
    public static SystemMessageOutputConfig effort(String effort) {
        return SystemMessageOutputConfig.builder().effort(effort).build();
    }
}
