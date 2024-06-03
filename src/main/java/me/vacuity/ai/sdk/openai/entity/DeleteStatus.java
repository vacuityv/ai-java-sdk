package me.vacuity.ai.sdk.openai.entity;

import lombok.Data;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-19 17:49
 **/
@Data
public class DeleteStatus {


    private String id;

    private String object;

    private Boolean deleted;
}
