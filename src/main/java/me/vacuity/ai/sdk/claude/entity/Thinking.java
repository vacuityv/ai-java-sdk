package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-02-25 09:13
 **/

@Data
@Builder
@AllArgsConstructor
public class Thinking {
    
    private String type;

    @JsonProperty("budget_tokens")
    private Integer budgetTokens;
}
