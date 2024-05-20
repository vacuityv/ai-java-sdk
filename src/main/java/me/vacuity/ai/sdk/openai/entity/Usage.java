package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-05-16 17:43
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usage {

    @JsonProperty("completion_tokens")
    private Integer completionTokens;

    @JsonProperty("prompt_tokens")
    private Integer promptTokens;

    @JsonProperty("total_tokens")
    private Integer totalTokens;
}
