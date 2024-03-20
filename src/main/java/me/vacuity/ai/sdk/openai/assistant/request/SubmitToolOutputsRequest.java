package me.vacuity.ai.sdk.openai.assistant.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 10:53
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitToolOutputsRequest {
    
    @JsonProperty("tool_outputs")
    private List<ToolOutput> toolOutputs;

    private Boolean stream;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolOutput {
        
        @JsonProperty("tool_call_id")
        private String toolCallId;
        
        private String output;
    }
}
