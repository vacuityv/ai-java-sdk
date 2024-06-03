package me.vacuity.ai.sdk.gemini.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.gemini.entity.Usage;

import java.util.List;

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

    private Usage usageMetadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromptFeedback {

        private List<ChatResponseSafetyRating> safetyRatings;
    }
}
