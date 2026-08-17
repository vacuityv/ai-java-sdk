package me.vacuity.ai.sdk.openai.file.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-19 17:49
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenaiFile {

    private String id;

    private Integer bytes;

    @JsonProperty("created_at")
    private Integer createdAt;

    private String filename;

    private String object;

    private String purpose;
}
