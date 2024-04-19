package me.vacuity.ai.sdk.openai.assistant.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-20 09:14
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantMessageDelta {

    private String id;

    private String object;

    private Delta delta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Delta {

        private String role;

        private List<AssistantMessageContent> content;

        @JsonProperty("file_ids")
        private List<String> file_ids;
    }
}
