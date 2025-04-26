package me.vacuity.ai.sdk.openai.image.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-04-25 13:55
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageInputTokensDetails {

    @JsonProperty("image_tokens")
    private Integer imageTokens;

    @JsonProperty("text_tokens")
    private Integer textTokens;
}
