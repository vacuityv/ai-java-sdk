package me.vacuity.ai.sdk.openai.assistant.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.entity.ChatTool;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 10:40
 **/

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThreadAndRunRequest {

    @JsonProperty("assistant_id")
    private String assistantId;

    private ThreadRequest thread;

    private String model;

    private String instructions;

    private List<ChatTool> tools;

    private Map<String, Object> metadata;

    private Boolean stream;
}
