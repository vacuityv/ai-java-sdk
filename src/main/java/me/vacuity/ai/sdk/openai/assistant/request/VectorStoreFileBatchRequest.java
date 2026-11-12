package me.vacuity.ai.sdk.openai.assistant.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-04-19 15:28
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorStoreFileBatchRequest {

    @JsonProperty("file_ids")
    private List<String> fileIds;

    /** Per-file entries ({@code file_id}, {@code attributes}, {@code chunking_strategy}), as an alternative to a flat file_ids list. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("files")
    private java.util.List<java.util.Map<String, Object>> files;
    /** Attributes applied to every file in the batch. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("attributes")
    private java.util.Map<String, Object> attributes;
    /** Chunking applied to every file in the batch. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("chunking_strategy")
    private me.vacuity.ai.sdk.openai.entity.ChunkingStrategy chunkingStrategy;
}
