package com.duck.warehouse.exception;

/** Maps to HTTP 400. */
public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) { super(message); }
}

