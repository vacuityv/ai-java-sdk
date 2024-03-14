package me.vacuity.ai.sdk.openai.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:46
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseError {

    public ChatResponseErrorDetails error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatResponseErrorDetails {

        String message;

        String type;

        String param;

        String code;
    }
}
