package me.vacuity.ai.sdk.openai.image.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-06-03 16:27
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageUsage {

    @JsonProperty("input_tokens")
    private Integer inputTokens;
    
    @JsonProperty("input_tokens_details")
    private ImageInputTokensDetails inputTokensDetails;

    @JsonProperty("output_tokens")
    private Integer outputTokens;
    
    @JsonProperty("total_tokens")
    private Integer totalTokens;
}
