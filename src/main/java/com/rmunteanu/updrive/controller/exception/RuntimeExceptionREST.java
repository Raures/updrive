package com.rmunteanu.updrive.controller.exception;

import org.springframework.http.HttpStatus;

public class RuntimeExceptionREST extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String message;

    protected RuntimeExceptionREST(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
