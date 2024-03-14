package me.vacuity.ai.sdk.openai.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatFunctionCall {

    private Integer index;

    private String id;

    private String type;

    private ChatFunctionDetail function;


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatFunctionDetail {

        private String name;

        private JsonNode arguments;
    }

}
