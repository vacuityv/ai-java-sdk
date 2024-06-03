package me.vacuity.ai.sdk.gemini.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-06-03 15:47
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Usage {

    private Integer promptTokenCount;
    
    private Integer candidatesTokenCount;
    
    private Integer totalTokenCount;
}
