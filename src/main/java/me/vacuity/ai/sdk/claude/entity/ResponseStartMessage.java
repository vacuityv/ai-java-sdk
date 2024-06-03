package me.vacuity.ai.sdk.claude.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-06-03 16:43
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseStartMessage {

    private String id;

    private Usage usage;
}
