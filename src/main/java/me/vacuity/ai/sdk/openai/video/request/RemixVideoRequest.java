package me.vacuity.ai.sdk.openai.video.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: Request for remixing a video with Sora API
 * @author: vacuity
 * @create: 2025-10-11
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemixVideoRequest {

    /**
     * Updated text prompt that directs the remix generation.
     * Required.
     */
    private String prompt;
}
