package com.carbontrace.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body of {@code POST /api/auth/forgot-password}
 * (COMMANDO.md Section 8.1).
 *
 * <p>The endpoint always answers 200 with a generic message, even for an unknown
 * address (Section 9: no account enumeration). That is a service-layer
 * responsibility — validating the email FORMAT here is still correct, because a
 * malformed address is a bad request rather than a hidden lookup miss.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;
}
