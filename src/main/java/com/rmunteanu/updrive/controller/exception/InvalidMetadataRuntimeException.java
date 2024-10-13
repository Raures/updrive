package com.rmunteanu.updrive.controller.exception;

import org.springframework.http.HttpStatus;

public class InvalidMetadataRuntimeException extends RuntimeExceptionREST {

    public InvalidMetadataRuntimeException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public InvalidMetadataRuntimeException(String message, Object... args) {
        super(HttpStatus.BAD_REQUEST, String.format(message, args));
    }

}
