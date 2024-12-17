package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-12-16 15:05
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tool {

    @JsonProperty("code_execution")
    private CodeExcution codeExcution;

    private List<ChatFunction> functionDeclarations;
}
