package com.rmunteanu.updrive.controller.exception;

import org.springframework.http.HttpStatus;

public class NoFileRuntimeException extends RuntimeExceptionREST {

    public NoFileRuntimeException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public NoFileRuntimeException(String message, Object... args) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, String.format(message, args));
    }

}
