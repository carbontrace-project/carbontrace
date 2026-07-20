package com.carbontrace.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import com.carbontrace.common.ApiResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Maps every exception to the {@link ApiResponse} error format
 * (COMMANDO.md Section 23).
 *
 * <p>Each handler returns {@code ApiResponse.error(message)}, so error bodies are
 * always {@code {"success": false, "message": "...", "data": null}}.
 *
 * <p>Logging policy: client-caused failures (4xx) are logged at WARN with the
 * message only; server- and upstream-caused failures (500/502) are logged at
 * ERROR with the full stack trace. Never log passwords, OTP codes, JWTs, AWS
 * credentials, or full presigned URLs (COMMANDO.md Section 10).
 *
 * <p>Module-specific exceptions (ShipmentException, PurchaseException) are added
 * alongside these handlers when their modules are built.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String AI_SERVICE_UNAVAILABLE = "AI service unavailable — please try again";
    private static final String UNEXPECTED_ERROR = "An unexpected error occurred";
    private static final String VALIDATION_FAILED = "Validation failed";

    /** 404 — requested entity does not exist. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** 400 — business rule violation (COMMANDO.md Section 9). */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** 401 — caller is not authenticated or credentials are invalid. */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(UnauthorizedException ex) {
        log.warn("Unauthorized: {}", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /** 403 — authenticated but the role is not permitted (COMMANDO.md Section 10). */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /**
     * 400 — Jakarta validation failure on a request DTO. Only the FIRST field
     * error message is surfaced, per COMMANDO.md Section 23.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(VALIDATION_FAILED);
        log.warn("Validation failed: {}", message);
        return build(HttpStatus.BAD_REQUEST, message);
    }

    /** 409 — unique/foreign-key constraint violation. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "Operation conflicts with existing data");
    }

    /**
     * 502 — the FastAPI service could not be reached or failed.
     * {@link ResourceAccessException} is a subclass of {@link RestClientException};
     * both are listed so the mapping stays explicit against COMMANDO.md Section 23.
     */
    @ExceptionHandler({ResourceAccessException.class, RestClientException.class})
    public ResponseEntity<ApiResponse<Object>> handleAiServiceFailure(RestClientException ex) {
        log.error("AI service call failed", ex);
        return build(HttpStatus.BAD_GATEWAY, AI_SERVICE_UNAVAILABLE);
    }

    /** 500 — catch-all for anything not mapped above. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR);
    }

    private ResponseEntity<ApiResponse<Object>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiResponse.error(message));
    }
}
