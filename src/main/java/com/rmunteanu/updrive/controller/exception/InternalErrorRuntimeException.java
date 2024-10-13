package com.rmunteanu.updrive.controller.exception;

import org.springframework.http.HttpStatus;

public class InternalErrorRuntimeException extends RuntimeExceptionREST {

    public InternalErrorRuntimeException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

}
