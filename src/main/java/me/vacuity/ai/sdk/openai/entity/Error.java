package me.vacuity.ai.sdk.openai.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-10-11 13:39
 **/


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Error {
    
    private String code;
    
    private String message;
}
