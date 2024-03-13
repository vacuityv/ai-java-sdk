package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-13 15:37
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Model {

    private String id;

    private Integer created;

    @JsonProperty("owned_by")
    private String ownedBy;
}
