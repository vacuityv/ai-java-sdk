package me.vacuity.ai.sdk.openai.image.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 18:25
 **/


@Data
@Builder
public class ImageVariationRequest {

    private String model;

    private Integer n;
    
    @JsonProperty("response_format")
    private String responseFormat;
    
    private String size;

    private String user;
}
