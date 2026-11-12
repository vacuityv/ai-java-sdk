package me.vacuity.ai.sdk.openai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.openai.entity.ChatFunctionCall;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-13 14:25
 **/


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponseMessage {

    private Object content;
    
    @JsonProperty("reasoning_content")
    private String reasoningContent;


    @JsonProperty("tool_calls")
    private List<ChatFunctionCall> toolCalls;

    /** "assistant" on a message; on a stream delta it appears on the first chunk only. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("role")
    private String role;
    /** Why the model declined. When set, {@code content} is null — check this first, or a refusal reads as an empty answer. Before 2.7.0 the SDK could not read it. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("refusal")
    private String refusal;
    /** Citations from web search. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("annotations")
    private java.util.List<me.vacuity.ai.sdk.openai.entity.ChatAnnotation> annotations;
    /** Audio output, when the request asked for an audio modality. @since 2.7.0 */
    @com.fasterxml.jackson.annotation.JsonProperty("audio")
    private me.vacuity.ai.sdk.openai.entity.ChatAudioOutput audio;
}
