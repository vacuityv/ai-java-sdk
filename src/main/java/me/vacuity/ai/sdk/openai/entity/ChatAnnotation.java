package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An annotation on a chat-completions message — currently a URL citation from
 * web search.
 *
 * @author vacuity
 * @since 2.7.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAnnotation {

    /** "url_citation". */
    private String type;

    @JsonProperty("url_citation")
    private UrlCitation urlCitation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UrlCitation {
        /** Character offset into the message content where the citation starts. */
        @JsonProperty("start_index")
        private Integer startIndex;

        @JsonProperty("end_index")
        private Integer endIndex;

        private String url;

        private String title;
    }
}
