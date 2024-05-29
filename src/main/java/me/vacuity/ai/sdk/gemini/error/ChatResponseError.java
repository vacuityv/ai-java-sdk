package me.vacuity.ai.sdk.gemini.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:46
 **/


@Data
public class ChatResponseError {
    
    private ChatResponseErrorDetail error;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatResponseErrorDetail{
        
        private String code;
        
        private String message;
        
        private String status;
    }
}
