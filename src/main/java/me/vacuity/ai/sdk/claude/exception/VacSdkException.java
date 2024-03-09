package me.vacuity.ai.sdk.claude.exception;

import lombok.Data;
import me.vacuity.ai.sdk.claude.error.ChatResponseError;

import java.io.Serializable;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-03-06 10:59
 **/


@Data
public class VacSdkException extends RuntimeException implements Serializable {

    private String code;

    private String msg;
    
    private ChatResponseError detail;
    
    public VacSdkException(String code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public VacSdkException(String code, String msg, ChatResponseError detail) {
        super(msg);
        this.code = code;
        this.msg = msg;
        this.detail = detail;
    }
}
