package me.vacuity.ai.sdk.openai.batch.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-04-29 17:53
 **/

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateBatchRequest {

    @Builder.Default
    @JsonProperty("completion_window")
    private String completionWindow = "24h";

    private String endpoint;

    @JsonProperty("input_file_id")
    private String inputFileId;

    private Map<String, Object> metadata;
}
