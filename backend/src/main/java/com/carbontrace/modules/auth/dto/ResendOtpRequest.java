package com.carbontrace.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body of {@code POST /api/auth/resend-otp} (COMMANDO.md Section 8.1).
 *
 * <p>Section 9: issuing a new code marks every previous unused code for that
 * (email, purpose) as used — enforced by OtpService at STEP A010.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResendOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Purpose is required")
    @Pattern(regexp = "^(REGISTRATION|PASSWORD_RESET)$",
             message = "Purpose must be REGISTRATION or PASSWORD_RESET")
    private String purpose;
}
