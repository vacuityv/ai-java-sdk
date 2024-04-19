package me.vacuity.ai.sdk.openai.assistant.entity.inner;

import lombok.Builder;
import lombok.Data;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-04-19 14:44
 **/


@Data
@Builder
public class ExpiresAfter {

    private String anchor;

    private Integer days;
}
