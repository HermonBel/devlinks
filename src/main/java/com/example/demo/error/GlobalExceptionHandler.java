package com.example.demo.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors from @Valid on @RequestBody.
     * Triggered when a field violates @NotBlank, @Size, @URL, etc.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                fieldErrors.put(err.getField(), err.getDefaultMessage())
        );

        ApiError body = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "One or more fields are invalid",
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Catch-all for anything else. Prevents leaking stack traces to clients.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        ApiError body = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.internalServerError().body(body);
    }
}