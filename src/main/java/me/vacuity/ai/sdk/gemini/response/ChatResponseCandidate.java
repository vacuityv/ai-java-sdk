package me.vacuity.ai.sdk.gemini.response;

import lombok.Data;
import me.vacuity.ai.sdk.gemini.entity.ChatMessage;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-09 13:32
 **/

@Data
public class ChatResponseCandidate {

    private ChatMessage content;

    private String finishReason;

    private Integer index;

    private List<ChatResponseSafetyRating> safetyRatings;

    private GroundingMetadata groundingMetadata;

    /** A human-readable reason the candidate stopped, alongside {@code finishReason}. @since 2.7.0 */
    private String finishMessage;
    /** Sources the output recites. @since 2.7.0 */
    private me.vacuity.ai.sdk.gemini.entity.CitationMetadata citationMetadata;
    /** What the url-context tool fetched. @since 2.7.0 */
    private me.vacuity.ai.sdk.gemini.entity.UrlContextMetadata urlContextMetadata;
    /** Tokens in this candidate. @since 2.7.0 */
    private Integer tokenCount;
    /** Attributions for grounded answers. @since 2.7.0 */
    private java.util.List<java.util.Map<String, Object>> groundingAttributions;
    /** Pass back on the next request to resume a paused generation. @since 2.7.0 */
    private String continuationToken;
    /** Average log probability over the candidate's tokens. @since 2.7.0 */
    private Double avgLogprobs;
    /** Per-token log probabilities, when {@code responseLogprobs} was set. Before 2.7.0 they were requested and dropped. @since 2.7.0 */
    private me.vacuity.ai.sdk.gemini.entity.LogprobsResult logprobsResult;
}
