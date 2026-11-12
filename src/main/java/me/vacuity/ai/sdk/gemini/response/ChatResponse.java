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

    /** Server id for this response — present on every live response, dropped by the SDK before 2.7.0. @since 2.7.0 */
    private String responseId;
    /** The exact model version that served the request, for example "gemini-3-flash-preview". @since 2.7.0 */
    private String modelVersion;
    /** The serving model's lifecycle stage, when the server reports it. @since 2.7.0 */
    private me.vacuity.ai.sdk.gemini.entity.ModelStatus modelStatus;
}
