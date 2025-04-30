package me.vacuity.ai.sdk.openai.batch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-04-29 18:08
 **/

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchRequestOutputObject {


    @JsonProperty("custom_id")
    private String customId;

    private BatchRequestOutputObjectError error;

    private String id;

    private BatchRequestOutputObjectResponse response;
}
