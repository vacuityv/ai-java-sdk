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
    
}
