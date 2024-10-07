package com.rmunteanu.updrive.controller.advice;

import com.rmunteanu.updrive.controller.exception.RuntimeExceptionREST;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeExceptionREST.class)
    public ResponseEntity<String> handle(RuntimeExceptionREST exception) {
        return new ResponseEntity<>(exception.getMessage(), exception.getHttpStatus());
    }

}
