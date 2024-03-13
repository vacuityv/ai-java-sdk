package me.vacuity.ai.sdk.openai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.entity.ChatMessage;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-13 14:43
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseChoice {

    @JsonProperty("finish_reason")
    private String finishReason;

    private Integer index;

    private ChatMessage message;

    private ChatMessage delta;
}
