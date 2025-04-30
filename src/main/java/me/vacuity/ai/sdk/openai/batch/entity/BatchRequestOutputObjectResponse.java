package me.vacuity.ai.sdk.openai.batch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-04-29 18:08
 **/

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchRequestOutputObjectResponse {


    private Map<String, Object> body;

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("status_code")
    private Integer statusCode;
}
