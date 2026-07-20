package com.carbontrace.exception;

/**
 * Thrown when a requested entity does not exist. Mapped to HTTP 404 by
 * {@link GlobalExceptionHandler} (COMMANDO.md Section 23).
 *
 * <p>Raised from repository lookups via
 * {@code orElseThrow(() -> new ResourceNotFoundException(...))} per COMMANDO.md
 * Section 21.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
