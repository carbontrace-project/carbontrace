package com.carbontrace.exception;

/**
 * Thrown when a caller is not authenticated or presents invalid credentials.
 * Mapped to HTTP 401 by {@link GlobalExceptionHandler} (COMMANDO.md Section 23).
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
