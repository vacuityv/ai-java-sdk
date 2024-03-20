package me.vacuity.ai.sdk.openai.assistant.entity.inner;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 10:15
 **/

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RunTooCall {

    private String id;
    
    private String type;
    
    @JsonProperty("code_interpreter")
    private RunCodeInterpreter codeInterpreter;

    private Map<String, Object> retrieval;
    
    private RunFunction function;
}
