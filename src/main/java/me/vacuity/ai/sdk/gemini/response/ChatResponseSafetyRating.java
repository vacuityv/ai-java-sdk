package me.vacuity.ai.sdk.gemini.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-09 13:35
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseSafetyRating {

    private String category;

    private String probability;
}
