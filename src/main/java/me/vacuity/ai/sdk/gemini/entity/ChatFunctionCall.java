package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatFunctionCall {

    private String name;

    private JsonNode args;

}
