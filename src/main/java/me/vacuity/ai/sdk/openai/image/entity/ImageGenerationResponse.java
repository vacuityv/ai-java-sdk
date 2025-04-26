package me.vacuity.ai.sdk.openai.image.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-04-25 13:52
 **/


@Data
@Builder
public class ImageGenerationResponse {

    @JsonProperty("created")
    private Integer created;

    @JsonProperty("data")
    private List<ImageData> data;

    @JsonProperty("usage")
    private ImageUsage usage;
}
