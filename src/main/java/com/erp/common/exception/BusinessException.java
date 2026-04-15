package com.erp.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}
