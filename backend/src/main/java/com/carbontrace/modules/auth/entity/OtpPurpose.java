package com.carbontrace.modules.auth.entity;

/**
 * Why an OTP was issued (COMMANDO.md Section 9).
 *
 * <p>Codes are scoped per (email, purpose): issuing a REGISTRATION code never
 * invalidates a PASSWORD_RESET code and vice versa.
 */
public enum OtpPurpose {
    REGISTRATION,
    PASSWORD_RESET
}
