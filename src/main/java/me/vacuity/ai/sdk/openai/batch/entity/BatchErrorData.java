package me.vacuity.ai.sdk.openai.batch.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-04-29 17:45
 **/

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchErrorData {

    private String code;

    private Integer line;

    private String message;

    private String param;
}
