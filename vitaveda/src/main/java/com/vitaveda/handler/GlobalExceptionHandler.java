package com.vitaveda.handler;

import com.vitaveda.model.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Response> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;

        if (message.contains("invalid user_name") || message.contains("invalid username")) {
            status = HttpStatus.NOT_FOUND;
        } else if (message.contains("complexity requirements") || message.contains("already taken")) {
            status = HttpStatus.BAD_REQUEST;
        }

        Response errorDetails = new Response(message, status.value());
        return new ResponseEntity<>(errorDetails, status);
    }
}
