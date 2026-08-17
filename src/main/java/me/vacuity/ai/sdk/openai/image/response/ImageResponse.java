package me.vacuity.ai.sdk.openai.image.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.image.entity.ImageData;
import me.vacuity.ai.sdk.openai.image.entity.ImageUsage;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-04-26 18:20
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {

    private Integer created;

    private List<ImageData> data;
    
    private ImageUsage usage;
}
