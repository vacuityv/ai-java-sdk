package me.vacuity.ai.sdk.claude.error;

import lombok.Data;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:46
 **/


@Data
public class ChatResponseError {

    private String type;

    private ChatResponseErrorDetail error;

    @Data
    public static class ChatResponseErrorDetail {

        private String type;

        private String message;
    }
}
