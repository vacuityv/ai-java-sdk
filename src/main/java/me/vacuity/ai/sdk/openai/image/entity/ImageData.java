package me.vacuity.ai.sdk.openai.image.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 18:14
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageData {

    @JsonProperty("b64_json")
    private String b64Json;

    private String url;

    @JsonProperty("revised_prompt")
    private String revisedPrompt;

    /**
     * Server-side id for this generated image, returned by gpt-image-2.6 on
     * generations and edits. Absent on SVG output.
     */
    @JsonProperty("generation_id")
    private String generationId;
}
