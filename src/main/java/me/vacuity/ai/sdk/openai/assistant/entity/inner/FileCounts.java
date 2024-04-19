package me.vacuity.ai.sdk.openai.assistant.entity.inner;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-04-19 14:46
 **/


@Data
@Builder
public class FileCounts {

    @JsonProperty("in_progress")
    private Integer inProgress;

    private Integer completed;

    private Integer failed;

    private Integer cancelled;

    private Integer total;
}
