package me.vacuity.ai.sdk.openai.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-10-11 13:39
 **/


@Data
@Builder
public class Error {
    
    private String code;
    
    private String message;
}
