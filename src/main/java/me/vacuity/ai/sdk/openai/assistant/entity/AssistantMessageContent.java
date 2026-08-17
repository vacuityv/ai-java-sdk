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
 * @create: 2024-03-20 09:19
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantMessageContent {

    private Integer index;

    private String type;

    @JsonProperty("image_file")
    private FileObject imageFile;

    private TextObject text;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileObject {

        @JsonProperty("file_id")
        private String fileId;

    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextObject {

        private String value;

        private List<TextAnnotation> annotations;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TextAnnotation {

            private Integer index;

            private String type;

            private String text;

            @JsonProperty("start_index")
            private Integer startIndex;

            @JsonProperty("end_index")
            private Integer endIndex;

            @JsonProperty("file_path")
            private FileObject filePath;

            @JsonProperty("file_citation")
            private FileCitation fileCitation;

            @Data
            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            public static class FileCitation {

                @JsonProperty("file_id")
                private String fileId;

                private String quote;
            }
        }
    }


}
