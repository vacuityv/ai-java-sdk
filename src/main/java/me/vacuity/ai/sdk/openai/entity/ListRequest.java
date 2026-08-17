package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-19 18:12
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
