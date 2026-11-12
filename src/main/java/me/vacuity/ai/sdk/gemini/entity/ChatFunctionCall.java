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

    /** Identifies this call, so the matching {@code FunctionResponse.id} can be paired with it when several calls run in parallel. @since 2.7.0 */
    private String id;
}
