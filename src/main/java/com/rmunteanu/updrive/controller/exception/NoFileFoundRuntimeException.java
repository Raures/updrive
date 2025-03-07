package com.rmunteanu.updrive.controller.exception;

import org.springframework.http.HttpStatus;

public class NoFileFoundRuntimeException extends RuntimeExceptionREST {

    public NoFileFoundRuntimeException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public NoFileFoundRuntimeException(String message, Object... args) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, String.format(message, args));
    }

}
