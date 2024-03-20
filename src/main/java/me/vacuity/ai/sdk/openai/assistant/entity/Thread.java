package me.vacuity.ai.sdk.openai.assistant.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-19 18:23
 **/


@Data
@Builder
public class Thread {

    private String id;

    private String object;

    @JsonProperty("created_at")
    private Integer createdAt;

    private Map<String, Object> metadata;
}
