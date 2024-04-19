package me.vacuity.ai.sdk.openai.assistant.entity.inner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 10:14
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RunCodeInterpreterOutput {

    private String type;

    private String logs;

    private RunImage image;
}
