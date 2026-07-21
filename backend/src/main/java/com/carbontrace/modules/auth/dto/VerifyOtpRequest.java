package com.carbontrace.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Request body of {@code POST /api/auth/verify-otp} (COMMANDO.md Section 8.1).
 *
 * <p>Section 9: the code is a 6-digit numeric string. Expiry, the 5-attempt
 * limit and single-use enforcement are stateful rules owned by OtpService
 * (STEP A010) — this class only rejects input that cannot possibly be a code.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    /**
     * Excluded from {@code toString}: an OTP is a single-use credential and must
     * never reach a log (COMMANDO.md Section 21).
     */
    @ToString.Exclude
    @NotBlank(message = "OTP code is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP code must be exactly 6 digits")
    private String code;

    /** String rather than the OtpPurpose enum — same reasoning as RegisterRequest.role. */
    @NotBlank(message = "Purpose is required")
    @Pattern(regexp = "^(REGISTRATION|PASSWORD_RESET)$",
             message = "Purpose must be REGISTRATION or PASSWORD_RESET")
    private String purpose;
}
