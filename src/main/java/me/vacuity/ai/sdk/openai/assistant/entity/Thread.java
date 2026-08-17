package me.vacuity.ai.sdk.openai.assistant.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.assistant.entity.inner.ToolResources;

import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-19 18:23
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Thread {

    private String id;

    private String object;

    @JsonProperty("created_at")
    private Integer createdAt;

    @JsonProperty("tool_resources")
    private ToolResources toolResources;

    private Map<String, Object> metadata;
}
