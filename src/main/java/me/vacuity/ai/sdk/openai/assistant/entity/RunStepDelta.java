package me.vacuity.ai.sdk.openai.assistant.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.assistant.entity.inner.RunStepDetails;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 09:52
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RunStepDelta {

    private String id;

    private String object;

    private Delta delta;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Delta {

        @JsonProperty("step_details")
        private RunStepDetails stepDetails;
    }
}
