package me.vacuity.ai.sdk.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.vacuity.ai.sdk.claude.error.ChatResponseError;

import java.io.Serializable;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:59
 **/


@Data
@NoArgsConstructor
public class VacSdkException extends RuntimeException implements Serializable {

    private String code;

    private String message;
    
    private Object detail;


    public VacSdkException(String code, String message, Object detail) {
        super(message);
        this.code = code;
        this.message = message;
        this.detail = detail;
    }
}
