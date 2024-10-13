package com.rmunteanu.updrive.controller.exception;

import org.springframework.http.HttpStatus;

public class SlotConsumedException extends RuntimeExceptionREST {

    public SlotConsumedException(String message, Object... args) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, String.format(message, args));
    }

}
