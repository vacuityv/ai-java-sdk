package me.vacuity.ai.sdk.openai.assistant.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.assistant.entity.inner.LastError;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-04-19 14:41
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorStoreFile {

    private String id;

    private String object;

    @JsonProperty("created_at")
    private String created_at;

    @JsonProperty("vector_store_id")
    private String vectorStoreId;

    private String status;

    @JsonProperty("last_error")
    private LastError lastError;

    /** Up to 16 key/value pairs for filtering search results. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("attributes")
    private java.util.Map<String, Object> attributes;
    /** How this file was chunked. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("chunking_strategy")
    private me.vacuity.ai.sdk.openai.entity.ChunkingStrategy chunkingStrategy;
    /** Bytes this file takes up in the store. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("usage_bytes")
    private Long usageBytes;
}
