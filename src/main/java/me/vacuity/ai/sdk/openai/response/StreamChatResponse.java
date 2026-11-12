package me.vacuity.ai.sdk.openai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.entity.Usage;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-13 14:51
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamChatResponse {

    private String id;

    private List<StreamChatResponseChoice> choices;

    private Integer created;

    private String model;

    @JsonProperty("system_fingerprint")
    private String systemFingerprint;

    private String object;

    private Usage usage;

    /**
     * The first choice's text content, or null.
     * <p>
     * Null-safe since 2.7.0: before that it threw a NullPointerException when
     * {@code choices} was null, and a ClassCastException when the content
     * was a list of parts rather than a string. It is also excluded from JSON
     * now — Jackson used to serialize it as a {@code single_content} key the
     * API does not have, so any ChatResponse written back out carried a field
     * that was never there.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getSingleContent() {
        if (getChoices() == null || getChoices().isEmpty()
                || getChoices().get(0).getDelta() == null) {
            return null;
        }
        Object content = getChoices().get(0).getDelta().getContent();
        return content instanceof String ? (String) content : null;
    }

    /** Moderation results, when moderation was requested. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("moderation")
    private java.util.Map<String, Object> moderation;
    /** Random padding that hides payload sizes; present unless {@code stream_options.include_obfuscation} is false. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("obfuscation")
    private String obfuscation;
    /** The tier that actually served the request. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("service_tier")
    private String serviceTier;
}
