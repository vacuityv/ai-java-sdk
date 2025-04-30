package me.vacuity.ai.sdk.openai.batch.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-04-29 18:08
 **/

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchRequestOutputObjectError {


    private String code;

    private String message;
}
