package me.vacuity.ai.sdk.openai.assistant.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.assistant.entity.inner.ExpiresAfter;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-04-19 14:42
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorStoreRequest {

    @JsonProperty("file_ids")
    private List<String> fileIds;

    private String name;

    @JsonProperty("expires_after")
    private ExpiresAfter expiresAfter;

    private Map<String, Object> metadata;

    /** A description of the store. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("description")
    private String description;
    /** How files added to the store are chunked. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("chunking_strategy")
    private me.vacuity.ai.sdk.openai.entity.ChunkingStrategy chunkingStrategy;
}
