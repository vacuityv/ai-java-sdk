package me.vacuity.ai.sdk.gemini.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-02-05 12:45
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroundingMetadata {

    private SearchEntryPoint searchEntryPoint;

    private List<GroundingChunk> groundingChunks;

    private List<GroundingSupport> groundingSupports;

    private List<String> webSearchQueries;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchEntryPoint {

        private String renderedContent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroundingChunk {

        private Web web;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Web {

        private String uri;

        private String title;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroundingSupport {

        private Segment segment;

        private List<Integer> groundingChunkIndices;

        private List<BigDecimal> confidenceScores;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Segment {

        private Integer startIndex;

        private Integer endIndex;

        private String text;
    }
}
