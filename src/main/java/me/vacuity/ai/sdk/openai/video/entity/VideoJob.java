package me.vacuity.ai.sdk.openai.video.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @description: Video data from Sora API response
 * @author: vacuity
 * @create: 2025-10-11
 **/

@Data
@Builder
public class VideoJob {

    @JsonProperty("completedAt")
    private Integer completed_at;

    @JsonProperty("createdAt")
    private Integer created_at;
    
    private Error error;

    @JsonProperty("expiresAt")
    private Integer expires_at;
    
    private String id;
    
    private String model;
    
    private String object;
    
    private Integer progress;

    @JsonProperty("remixedFromVideoId")
    private String remixed_from_video_id;
    
    private String seconds;
    
    private String size;
    
    private String status;
}
