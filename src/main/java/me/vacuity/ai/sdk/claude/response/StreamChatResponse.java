package me.vacuity.ai.sdk.claude.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.claude.entity.ChatMessageContent;
import me.vacuity.ai.sdk.claude.entity.ResponseStartMessage;
import me.vacuity.ai.sdk.claude.entity.Usage;
import me.vacuity.ai.sdk.claude.error.ChatResponseError;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:17
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StreamChatResponse {


    private String type;

    private ResponseStartMessage message;

    private ChatMessageContent delta;

    private ChatResponseError.ChatResponseErrorDetail error;

    private Usage usage;
}
