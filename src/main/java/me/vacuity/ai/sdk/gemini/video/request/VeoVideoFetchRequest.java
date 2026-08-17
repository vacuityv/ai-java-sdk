package me.vacuity.ai.sdk.gemini.video.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-10-16 16:56
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeoVideoFetchRequest {

    private String operationName;
}
