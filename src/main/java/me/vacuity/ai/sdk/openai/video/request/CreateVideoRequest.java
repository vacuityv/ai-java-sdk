package me.vacuity.ai.sdk.openai.video.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: Request for creating a video with Sora API
 * @author: vacuity
 * @create: 2025-10-11
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVideoRequest {

    
    private String prompt;

    
    private String model;

    
    private String seconds;

    
    private String size;
}
