package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * One token's log probability, with the most likely alternatives. OpenAI's
 * {@code ChatCompletionTokenLogprob}.
 *
 * @author vacuity
 * @since 2.7.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenLogprob {

    private String token;

    private Double logprob;

    /** UTF-8 bytes of the token, or null when it has no byte representation. */
    private List<Integer> bytes;

    /** The alternatives requested through {@code top_logprobs}. */
    @JsonProperty("top_logprobs")
    private List<TokenLogprob> topLogprobs;
}
