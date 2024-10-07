package com.rmunteanu.updrive.controller.exception;

import org.springframework.http.HttpStatus;

public class SlotExpiredRuntimeException extends RuntimeExceptionREST {

    public SlotExpiredRuntimeException(String message, Object... args) {
        super(HttpStatus.BAD_REQUEST, String.format(message, args));
    }

}
