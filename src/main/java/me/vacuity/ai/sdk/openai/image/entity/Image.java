package me.vacuity.ai.sdk.openai.image.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 18:14
 **/


@Data
@Builder
public class Image {

    @JsonProperty("b64_json")
    private String b64Json;

    private String url;
    
    @JsonProperty("revised_prompt")
    private String revisedPrompt;
}
