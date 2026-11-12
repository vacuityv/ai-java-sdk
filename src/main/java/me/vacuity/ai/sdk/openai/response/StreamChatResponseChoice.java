package me.vacuity.ai.sdk.openai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-13 14:43
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamChatResponseChoice {

    @JsonProperty("finish_reason")
    private String finishReason;

    private Integer index;

    private ChatResponseMessage delta;

    /** Log probabilities, when the request set {@code logprobs}. Before 2.7.0 they were requested and then dropped. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("logprobs")
    private me.vacuity.ai.sdk.openai.entity.ChoiceLogprobs logprobs;
}
