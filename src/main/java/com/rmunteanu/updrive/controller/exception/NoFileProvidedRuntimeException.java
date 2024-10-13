package com.rmunteanu.updrive.controller.exception;

import org.springframework.http.HttpStatus;

public class NoFileProvidedRuntimeException extends RuntimeExceptionREST {

    public NoFileProvidedRuntimeException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

}
