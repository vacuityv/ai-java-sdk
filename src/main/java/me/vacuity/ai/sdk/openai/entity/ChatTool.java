package me.vacuity.ai.sdk.openai.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-14 13:46
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatTool {

    private String type;

    private ChatFunction function;
}
