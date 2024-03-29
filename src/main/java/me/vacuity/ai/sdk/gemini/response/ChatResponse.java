package me.vacuity.ai.sdk.gemini.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.claude.entity.ChatMessageContent;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:17
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {

    private List<ChatResponseCandidate> candidates;

    private PromptFeedback promptFeedback;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromptFeedback {

        private List<ChatResponseSafetyRating> safetyRatings;
    }
}
