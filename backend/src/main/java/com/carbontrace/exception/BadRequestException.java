package com.carbontrace.exception;

/**
 * Thrown when a request violates a business rule (COMMANDO.md Section 9).
 * Mapped to HTTP 400 by {@link GlobalExceptionHandler} (COMMANDO.md Section 23).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
