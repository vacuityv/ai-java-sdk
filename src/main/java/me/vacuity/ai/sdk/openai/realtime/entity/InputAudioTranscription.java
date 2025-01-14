package me.vacuity.ai.sdk.openai.realtime.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-01-14 14:11
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InputAudioTranscription {

    private String model;
}
