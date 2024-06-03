package me.vacuity.ai.sdk.openai.image.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 18:16
 **/


@Data
@Builder
public class CreateImageRequest {

    private String prompt;

    private String model;

    private Integer n;

    private String quality;

    @JsonProperty("response_format")
    private String responseFormat;

    private String size;

    private String style;

    private String user;
}
