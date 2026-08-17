package me.vacuity.ai.sdk.openai.assistant.entity.inner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-04-19 14:44
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpiresAfter {

    private String anchor;

    private Integer days;
}
