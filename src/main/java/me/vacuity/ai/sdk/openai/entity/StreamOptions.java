package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-05-16 17:39
 **/


@Data
@Builder
public class StreamOptions {

    @JsonProperty("include_usage")
    private Boolean includeUsage;
}
