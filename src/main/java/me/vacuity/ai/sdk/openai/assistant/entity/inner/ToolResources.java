package me.vacuity.ai.sdk.openai.assistant.entity.inner;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-04-19 14:53
 **/


@Data
@Builder
public class ToolResources {

    @JsonProperty("code_interpreter")
    private CodeInterpreterResources codeInterpreter;

    @JsonProperty("file_search")
    private FileSearchResources fileSearch;


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CodeInterpreterResources {

        @JsonProperty("file_ids")
        private List<String> fileIds;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileSearchResources {

        @JsonProperty("vector_store_ids")
        private List<String> vectorStoreIds;

        @JsonProperty("vector_stores")
        private List<VectorStoresResources> vectorStores;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VectorStoresResources {

        @JsonProperty("file_ids")
        private List<String> fileIds;

        private Map<String, Object> metadata;
    }
}
