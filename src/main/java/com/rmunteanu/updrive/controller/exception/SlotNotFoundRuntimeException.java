package com.rmunteanu.updrive.controller.exception;

import org.springframework.http.HttpStatus;

public class SlotNotFoundRuntimeException extends RuntimeExceptionREST {

    public SlotNotFoundRuntimeException(String message, Object... args) {
        super(HttpStatus.NOT_FOUND, String.format(message, args));
    }

}
