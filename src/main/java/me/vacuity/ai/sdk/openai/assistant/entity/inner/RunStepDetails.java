package me.vacuity.ai.sdk.openai.assistant.entity.inner;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 10:18
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RunStepDetails {

    private String type;

    @JsonProperty("message_creation")
    private RunMessageCreation messageCreation;

    @JsonProperty("too_calls")
    private List<RunTooCall> tooCalls;
}
