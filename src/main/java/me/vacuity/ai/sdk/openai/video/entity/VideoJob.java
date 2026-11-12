package me.vacuity.ai.sdk.openai.video.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: Video data from Sora API response
 * @author: vacuity
 * @create: 2025-10-11
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoJob {

    // Before 2.7.0 these four were annotated with camelCase names
    // ("completedAt" and so on). An explicit @JsonProperty overrides the
    // client's snake_case strategy, so each one read a key the API never sends
    // and came back null. The spec names them completed_at, created_at,
    // expires_at and remixed_from_video_id.
    @JsonProperty("completed_at")
    private Integer completed_at;

    @JsonProperty("created_at")
    private Integer created_at;
    
    private Error error;

    @JsonProperty("expires_at")
    private Integer expires_at;
    
    private String id;
    
    private String model;
    
    private String object;
    
    private Integer progress;

    @JsonProperty("remixed_from_video_id")
    private String remixed_from_video_id;
    
    private String seconds;
    
    private String size;
    
    private String status;
}
