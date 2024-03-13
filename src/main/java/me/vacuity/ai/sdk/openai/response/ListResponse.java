package me.vacuity.ai.sdk.openai.response;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-13 15:49
 **/

@Data
public class ListResponse<T> {

    public String object;

    public List<T> data;
}
