package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * How a vector store splits files into chunks: "auto", or "static" with
 * explicit sizes.
 *
 * @author vacuity
 * @since 2.7.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChunkingStrategy {

    /** "auto" or "static". */
    private String type;

    /** static only. */
    @JsonProperty("static")
    private Static staticConfig;

    public static ChunkingStrategy auto() {
        return ChunkingStrategy.builder().type("auto").build();
    }

    public static ChunkingStrategy fixed(int maxChunkSizeTokens, int chunkOverlapTokens) {
        return ChunkingStrategy.builder().type("static")
                .staticConfig(new Static(maxChunkSizeTokens, chunkOverlapTokens)).build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Static {
        @JsonProperty("max_chunk_size_tokens")
        private Integer maxChunkSizeTokens;

        @JsonProperty("chunk_overlap_tokens")
        private Integer chunkOverlapTokens;
    }
}
