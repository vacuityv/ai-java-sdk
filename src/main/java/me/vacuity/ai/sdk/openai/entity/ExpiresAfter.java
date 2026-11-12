package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An expiry policy relative to an anchor time, as several OpenAI endpoints
 * take it.
 *
 * @author vacuity
 * @since 2.7.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpiresAfter {

    /** For example "created_at". */
    private String anchor;

    private Long seconds;
}
