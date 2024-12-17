package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-12-16 15:33
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionResponse {

    private String name;
    
    private JsonNode response;
}
