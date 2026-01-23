package me.vacuity.ai.sdk.openai.responses.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Content item in an output message.
 *
 * @author: vacuity
 * @create: 2025-01-22
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseOutputContent {

    /**
     * The type of the content.
     * Can be "output_text", "refusal", etc.
     */
    private String type;

    /**
     * The text content (for output_text type).
     */
    private String text;

    /**
     * The refusal message (for refusal type).
     */
    private String refusal;

    /**
     * Annotations for the text content.
     */
    private List<Annotation> annotations;

    /**
     * Log probabilities for the content.
     */
    private List<TokenLogprob> logprobs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Annotation {
        private String type;
        @JsonProperty("start_index")
        private Integer startIndex;
        @JsonProperty("end_index")
        private Integer endIndex;
        private String url;
        private String title;
        @JsonProperty("file_id")
        private String fileId;
        private String filename;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenLogprob {
        private String token;
        private Float logprob;
        private List<Integer> bytes;
        @JsonProperty("top_logprobs")
        private List<TopLogprob> topLogprobs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopLogprob {
        private String token;
        private Float logprob;
        private List<Integer> bytes;
    }
}
