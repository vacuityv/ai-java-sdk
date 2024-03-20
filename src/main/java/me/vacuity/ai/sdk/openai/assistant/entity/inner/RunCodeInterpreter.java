package me.vacuity.ai.sdk.openai.assistant.entity.inner;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 10:13
 **/


@Data
@Builder
public class RunCodeInterpreter {

    private String input;

    private List<RunCodeInterpreterOutput> outputs;
}
