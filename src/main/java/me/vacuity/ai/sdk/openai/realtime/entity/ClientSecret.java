package me.vacuity.ai.sdk.openai.realtime.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-01-14 14:10
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientSecret {

    private String value;

    @JsonProperty("expires_at")
    private Integer expiresAt;
}
