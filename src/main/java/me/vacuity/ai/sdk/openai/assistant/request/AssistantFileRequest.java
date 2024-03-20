package me.vacuity.ai.sdk.openai.assistant.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-19 18:11
 **/


@Data
@Builder
public class AssistantFileRequest {

    @JsonProperty("file_id")
    private String fileId;
}
