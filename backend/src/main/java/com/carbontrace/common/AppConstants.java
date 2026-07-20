package com.carbontrace.common;

/**
 * Shared constants used across every module (COMMANDO.md Section 11).
 */
public final class AppConstants {

    private AppConstants() {} // prevent instantiation

    // Strings, NOT ints: used as @RequestParam(defaultValue = ...) values,
    // and annotation attributes require compile-time String constants.
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";

    public static final String ROLE_AUDITOR = "ROLE_AUDITOR";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final int OTP_LENGTH = 6;
    public static final int OTP_EXPIRY_MINUTES = 10;
    public static final int OTP_MAX_ATTEMPTS = 5;

    public static final long MAX_UPLOAD_BYTES = 10L * 1024 * 1024; // 10 MB
    public static final int PRESIGNED_PUT_EXPIRY_MINUTES = 10;
    public static final int PRESIGNED_GET_EXPIRY_MINUTES = 15;
}
