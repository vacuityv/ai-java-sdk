package me.vacuity.ai.sdk.openai.image.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 18:16
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateImageRequest {

    private String prompt;
    
    private String background;

    private String model;
    
    private String moderation;

    private Integer n;

    @JsonProperty("output_compression")
    private Integer outputCompression;
    
    @JsonProperty("output_format")
    private String outputFormat;

    private String quality;

    @JsonProperty("response_format")
    private String responseFormat;

    private String size;

    private String style;

    private String user;
}
