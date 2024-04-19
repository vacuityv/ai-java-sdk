package me.vacuity.ai.sdk.openai.assistant.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-04-19 15:28
 **/


@Data
@Builder
public class VectorStoreFileBatchRequest {

    @JsonProperty("file_ids")
    private List<String> fileIds;
}
