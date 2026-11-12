package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-05-16 17:39
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamOptions {

    @JsonProperty("include_usage")
    private Boolean includeUsage;

    /** Set false to drop the {@code obfuscation} padding from stream chunks. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("include_obfuscation")
    private Boolean includeObfuscation;
}
