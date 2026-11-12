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
public class StreamChatResponse {

    private List<ChatResponseCandidate> candidates;

    private Usage usageMetadata;

    /** Server id for this response — present on every live response, dropped by the SDK before 2.7.0. @since 2.7.0 */
    private String responseId;
    /** The exact model version that served the request, for example "gemini-3-flash-preview". @since 2.7.0 */
    private String modelVersion;
    /** The serving model's lifecycle stage, when the server reports it. @since 2.7.0 */
    private me.vacuity.ai.sdk.gemini.entity.ModelStatus modelStatus;
    /** Why the prompt was blocked, if it was; arrives on the first chunk. @since 2.7.0 */
    private ChatResponse.PromptFeedback promptFeedback;
}
