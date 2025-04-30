package me.vacuity.ai.sdk.openai.batch.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-04-29 17:45
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchError {

    private List<BatchErrorData> data;

    private String object;
}
