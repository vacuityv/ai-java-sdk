package me.vacuity.ai.sdk.openai.assistant.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import me.vacuity.ai.sdk.openai.assistant.entity.inner.FileCounts;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-04-19 14:41
 **/


@Data
@Builder
public class VectorStoreFileBatch {

    private String id;

    private String object;

    @JsonProperty("created_at")
    private String created_at;

    @JsonProperty("vector_store_id")
    private String vectorStoreId;

    private String status;

    @JsonProperty("file_counts")
    private FileCounts fileCounts;
}
