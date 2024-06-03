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
public class EditImageRequest {

    private String prompt;

    private String model;

    private Integer n;

    private String size;

    @JsonProperty("response_format")
    private String responseFormat;

    private String user;
}
