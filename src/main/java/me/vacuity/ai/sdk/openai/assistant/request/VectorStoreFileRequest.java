package me.vacuity.ai.sdk.openai.assistant.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-04-19 15:28
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorStoreFileRequest {

    @JsonProperty("file_id")
    private String fileId;
}
