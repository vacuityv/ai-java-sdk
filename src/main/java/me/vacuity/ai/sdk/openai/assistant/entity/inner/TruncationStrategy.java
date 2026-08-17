package me.vacuity.ai.sdk.openai.assistant.entity.inner;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-04-19 15:09
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruncationStrategy {

    private String type;

    @JsonProperty("last_messages")
    private Integer lastMessages;
}
