package me.vacuity.ai.sdk.gemini.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-12-16 15:31
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeExecutionResult {

    private String outcome;

    private String output;
}
