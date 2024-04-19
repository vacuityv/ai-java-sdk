package me.vacuity.ai.sdk.openai.assistant.entity.inner;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-04-19 15:09
 **/


@Data
@Builder
public class TruncationStrategy {

    private String type;

    @JsonProperty("last_messages")
    private Integer lastMessages;
}
