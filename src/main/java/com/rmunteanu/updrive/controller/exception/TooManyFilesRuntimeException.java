package com.rmunteanu.updrive.controller.exception;

import org.springframework.http.HttpStatus;

public class TooManyFilesRuntimeException extends RuntimeExceptionREST {

    public TooManyFilesRuntimeException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

}
