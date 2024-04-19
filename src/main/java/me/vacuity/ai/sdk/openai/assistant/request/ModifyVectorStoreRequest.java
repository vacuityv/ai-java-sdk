package me.vacuity.ai.sdk.openai.assistant.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import me.vacuity.ai.sdk.openai.assistant.entity.inner.ExpiresAfter;

import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-04-19 14:42
 **/


@Data
@Builder
public class ModifyVectorStoreRequest {

    private String name;

    @JsonProperty("expires_after")
    private ExpiresAfter expiresAfter;

    private Map<String, Object> metadata;
}
