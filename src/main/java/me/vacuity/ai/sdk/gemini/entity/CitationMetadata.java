package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Sources the model's output recites.
 *
 * @author vacuity
 * @since 2.7.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitationMetadata {

    private List<CitationSource> citationSources;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CitationSource {
        private Integer startIndex;
        private Integer endIndex;
        private String uri;
        private String license;
    }
}
