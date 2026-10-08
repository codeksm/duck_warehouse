package com.duck.warehouse.exception;

/** Maps to HTTP 400. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}

