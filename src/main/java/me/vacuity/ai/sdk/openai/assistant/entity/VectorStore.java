package me.vacuity.ai.sdk.openai.assistant.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import me.vacuity.ai.sdk.openai.assistant.entity.inner.ExpiresAfter;
import me.vacuity.ai.sdk.openai.assistant.entity.inner.FileCounts;

import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-04-19 14:41
 **/


@Data
@Builder
public class VectorStore {

    private String id;

    private String object;

    @JsonProperty("created_at")
    private String created_at;

    private String name;

    private Integer bytes;

    @JsonProperty("file_counts")
    private FileCounts fileCounts;

    private String status;

    @JsonProperty("expires_after")
    private ExpiresAfter expiresAfter;

    @JsonProperty("expires_at")
    private Integer expiresAt;

    @JsonProperty("last_active_at")
    private Integer lastActiveAt;

    private Map<String, Object> metadata;
}
