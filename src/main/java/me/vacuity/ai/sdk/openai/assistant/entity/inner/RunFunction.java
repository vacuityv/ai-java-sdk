package me.vacuity.ai.sdk.openai.assistant.entity.inner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 10:09
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunFunction {

    private String name;

    private String arguments;

    private String output;
}
