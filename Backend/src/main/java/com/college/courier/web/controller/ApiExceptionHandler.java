
package com.college.courier.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(ApiExceptionHandler.class);

    private final ObjectMapper objectMapper;

    public ApiExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public void validation(
            MethodArgumentNotValidException e,
            HttpServletResponse response) throws IOException {

        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(x -> x.getField() + ": " + x.getDefaultMessage())
                .toList();

        writeJson(response, 400,
                Map.of("error", "Validation failed", "details", errors));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public void conflict(
            DataIntegrityViolationException e,
            HttpServletResponse response) throws IOException {

        logger.warn("Database constraint violation", e);

        writeJson(response, 409,
                Map.of("error",
                "Database constraint violation. Check duplicate phone numbers and referenced IDs."));
    }

    @ExceptionHandler(Exception.class)
    public void other(
            Exception e,
            HttpServletResponse response) throws IOException {

        logger.error("Unexpected application error", e);

        int status = 500;
        String message = "An unexpected server error occurred.";

        if (e instanceof ResponseStatusException r) {
            status = r.getStatusCode().value();
            message = r.getReason() == null
                    ? "Request failed"
                    : r.getReason();
        }

        writeJson(response, status, Map.of("error", message));
    }

    private void writeJson(
            HttpServletResponse response,
            int status,
            Map<String, Object> body) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
