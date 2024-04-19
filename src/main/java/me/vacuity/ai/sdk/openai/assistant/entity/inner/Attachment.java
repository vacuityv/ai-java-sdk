package me.vacuity.ai.sdk.openai.assistant.entity.inner;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import me.vacuity.ai.sdk.openai.entity.ChatTool;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-04-19 15:03
 **/


@Data
@Builder
public class Attachment {

    @JsonProperty("file_id")
    private String fileId;

    private List<ChatTool> tools;
}
