package com.example.demo.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final Map<String, String> fields;   // null unless it's a validation error

    public ApiError(int status, String error, String message, String path) {
        this(status, error, message, path, null);
    }

    public ApiError(int status, String error, String message, String path, Map<String, String> fields) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fields = fields;
    }

    // Getters
    public Instant getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public Map<String, String> getFields() { return fields; }
}