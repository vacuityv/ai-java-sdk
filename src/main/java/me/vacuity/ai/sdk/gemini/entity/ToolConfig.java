package me.vacuity.ai.sdk.gemini.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-12-16 14:48
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolConfig {

    private FunctionCallingConfig functionCallingConfig;
}
