package me.vacuity.ai.sdk.openai.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-14 13:46
 **/


@Data
@Builder
public class ChatTool {

    private String type;

    private ChatFunction function;
}
