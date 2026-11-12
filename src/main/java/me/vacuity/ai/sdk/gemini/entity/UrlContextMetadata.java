package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * What the url-context tool fetched, and whether each fetch worked.
 *
 * @author vacuity
 * @since 2.7.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlContextMetadata {

    private List<UrlMetadata> urlMetadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UrlMetadata {
        private String retrievedUrl;
        /** For example "URL_RETRIEVAL_STATUS_SUCCESS" or "URL_RETRIEVAL_STATUS_PAYWALL". */
        private String urlRetrievalStatus;
    }
}
