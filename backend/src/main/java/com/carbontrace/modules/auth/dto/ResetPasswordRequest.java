package com.carbontrace.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Request body of {@code POST /api/auth/reset-password}
 * (COMMANDO.md Section 8.1).
 *
 * <p>The reset code is the PASSWORD_RESET OTP delivered by forgot-password, so
 * {@code code} carries the same 6-digit rule as verify-otp, and
 * {@code newPassword} carries the same strength rules as registration — a reset
 * must not be a way around the Section 9 password policy.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    /** Excluded from {@code toString} (COMMANDO.md Section 21). */
    @ToString.Exclude
    @NotBlank(message = "OTP code is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP code must be exactly 6 digits")
    private String code;

    /** Excluded from {@code toString} (COMMANDO.md Section 21). */
    @ToString.Exclude
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
             message = "New password must contain at least one uppercase letter, one lowercase letter and one digit")
    private String newPassword;
}
