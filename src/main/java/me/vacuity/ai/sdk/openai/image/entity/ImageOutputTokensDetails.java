package me.vacuity.ai.sdk.openai.image.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Breakdown of the output tokens an image request produced.
 *
 * @author: vacuity
 * @create: 2026-11-05
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageOutputTokensDetails {

    @JsonProperty("image_tokens")
    private Integer imageTokens;

    @JsonProperty("text_tokens")
    private Integer textTokens;
}
