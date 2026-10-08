package com.duck.warehouse.exception;

/** Maps to HTTP 400. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}

