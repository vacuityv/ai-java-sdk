package me.vacuity.ai.sdk.gemini.exception;

import lombok.Data;
import me.vacuity.ai.sdk.gemini.error.ChatResponseError;

import java.io.Serializable;
import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:59
 **/


@Data
public class VacSdkException extends RuntimeException implements Serializable {

    private String code;

    private String msg;
    
    private List<ChatResponseError> details;
    
    public VacSdkException(String code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public VacSdkException(String code, String msg, List<ChatResponseError> details) {
        super(msg);
        this.code = code;
        this.msg = msg;
        this.details = details;
    }
}
