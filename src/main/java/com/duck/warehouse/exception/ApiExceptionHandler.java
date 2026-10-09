package com.duck.warehouse.exception;


import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


@RestControllerAdvice
public class ApiExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(InvalidInputException.class)
    ProblemDetail invalid(InvalidInputException e) { return problem(HttpStatus.BAD_REQUEST, e.getMessage()); }

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException e) { return problem(HttpStatus.NOT_FOUND, e.getMessage()); }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail conflict(ConflictException e) { return problem(HttpStatus.CONFLICT, e.getMessage()); }

    @ExceptionHandler(DuplicateKeyException.class)
    ProblemDetail duplicate(DuplicateKeyException e) {
        return problem(HttpStatus.CONFLICT,
                "An active duck with the same color, size and price already exists. Edit that duck's quantity instead.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> errors.putIfAbsent(f.getField(), f.getDefaultMessage()));
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setProperty("errors", errors);
        log.warn("Validation failed: {}", errors);
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail unreadable(HttpMessageNotReadableException e) {
        return problem(HttpStatus.BAD_REQUEST, "Malformed or unreadable JSON request body");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail typeMismatch(MethodArgumentTypeMismatchException e) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + e.getName() + "'");
    }
    
 // NEW: anything unexpected becomes a clean 500 and an ERROR log with the stack trace
    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> unexpected(Exception e) {
        if (e instanceof ErrorResponse er) {      // Spring's own 404/405/415 etc. keep their status
            return ResponseEntity.status(er.getStatusCode()).headers(er.getHeaders()).body(er.getBody());
        }
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"));
    }

    private static ProblemDetail problem(HttpStatus status, String detail) {
    	log.warn("Request rejected: {} - {}", status.value(), detail);   // all 
        return ProblemDetail.forStatusAndDetail(status, detail);
    }
}

