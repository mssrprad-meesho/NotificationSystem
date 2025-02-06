package org.example.notificationsystem.exception;

import org.example.notificationsystem.dto.response.Error;
import org.example.notificationsystem.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Error> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Validation failed");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("INVALID_REQUEST")
                .message(message)
                .build();

        Error response = Error.builder()
                .error(errorResponse)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Error> handleValidationException(ValidationException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ex.getMessage())
                .message(ex.getMessage())
                .build();

        Error response = Error.builder()
                .error(errorResponse)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Error> handleException(Exception ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("SERVER_ERROR")
                .message("Some Error Occurred. Either your request is malformed or it's something on our side.")
                .build();

        Error response = Error.builder()
                .error(errorResponse)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}