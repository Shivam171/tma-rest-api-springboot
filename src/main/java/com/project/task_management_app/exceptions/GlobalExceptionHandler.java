package com.project.task_management_app.exceptions;

import com.project.task_management_app.payload.Response.APIResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Handle ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        APIResponse<Object> response = new APIResponse<>();
        response.setSuccess(false);
        response.setMessage(ex.getMessage());
        response.setStatusCode(HttpStatus.NOT_FOUND.value());
        response.setPath(request.getRequestURI());
        response.setTimestamp(LocalDateTime.now().format(DATE_FORMATTER));

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Handle TaskAlreadyExistsException
    @ExceptionHandler(TaskAlreadyExistsException.class)
    public ResponseEntity<APIResponse<Object>> handleTaskAlreadyExistsException(
            TaskAlreadyExistsException ex,
            HttpServletRequest request) {

        APIResponse<Object> response = new APIResponse<>();
        response.setSuccess(false);
        response.setMessage(ex.getMessage());
        response.setStatusCode(HttpStatus.CONFLICT.value());
        response.setPath(request.getRequestURI());
        response.setTimestamp(LocalDateTime.now().format(DATE_FORMATTER));

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // Handle InvalidRequestException
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<APIResponse<Object>> handleInvalidRequestException(
            InvalidRequestException ex,
            HttpServletRequest request) {

        APIResponse<Object> response = new APIResponse<>();
        response.setSuccess(false);
        response.setMessage(ex.getMessage());
        response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        response.setPath(request.getRequestURI());
        response.setTimestamp(LocalDateTime.now().format(DATE_FORMATTER));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        APIResponse<Map<String, String>> response = new APIResponse<>();
        response.setSuccess(false);
        response.setMessage("Validation failed");
        response.setData(errors);
        response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        response.setPath(request.getRequestURI());
        response.setTimestamp(LocalDateTime.now().format(DATE_FORMATTER));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handle general exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Object>> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        APIResponse<Object> response = new APIResponse<>();
        response.setSuccess(false);
        response.setMessage("An unexpected error occurred: " + ex.getMessage());
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setPath(request.getRequestURI());
        response.setTimestamp(LocalDateTime.now().format(DATE_FORMATTER));

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}