package me.vacuity.ai.sdk.openai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.entity.Usage;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-13 14:51
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String id;

    private List<ChatResponseChoice> choices;

    private Integer created;

    private String model;

    @JsonProperty("system_fingerprint")
    private String systemFingerprint;

    private String object;

    private Usage usage;
    

    public String getSingleContent() {
        if (this == null || this.getChoices().size() == 0 || this.getChoices().get(0).getMessage() == null) {
            return null;
        } else {
            return (String) this.getChoices().get(0).getMessage().getContent();
        }
    }
}
