package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-19 18:12
 **/


@Data
@Builder
public class ListRequest {

    private Integer limit;

    private OrderBy order;

    private String after;

    private String before;

    public enum OrderBy {
        @JsonProperty("asc")
        ASCENDING,

        @JsonProperty("desc")
        DESCENDING
    }
}
