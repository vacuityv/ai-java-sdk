package me.vacuity.ai.sdk.openai.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Log probabilities for a choice, present when the request set
 * {@code logprobs = true}.
 * <p>
 * Before 2.7.0 the SDK could send {@code logprobs} / {@code top_logprobs} but
 * had nowhere to put the answer, so the values were requested, billed and then
 * dropped.
 *
 * @author vacuity
 * @since 2.7.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChoiceLogprobs {

    /** Per-token log probabilities of the message content. */
    private List<TokenLogprob> content;

    /** Per-token log probabilities of a refusal, when the model refused. */
    private List<TokenLogprob> refusal;
}
