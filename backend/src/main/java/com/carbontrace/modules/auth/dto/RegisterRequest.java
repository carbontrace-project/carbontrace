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
 * Request body of {@code POST /api/auth/register} (COMMANDO.md Section 8.1).
 *
 * <p>Validation mirrors the Section 9 registration rules. The {@code @Size(max)}
 * caps match the Section 7 column widths, so an over-long value is rejected as a
 * 400 with a clear message instead of surfacing as a 409 from the database.
 *
 * <p>Uniqueness of {@code email} is NOT expressible here — it is a database
 * lookup, enforced by AuthService at STEP A011.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    /**
     * Plaintext only in transit — hashed with BCrypt before storage, and excluded
     * from {@code toString} so a logged request body can never expose it
     * (COMMANDO.md Section 21).
     *
     * <p>Section 9: minimum 8 characters, at least one uppercase, one lowercase
     * and one digit. The lookaheads assert the three character classes; the
     * length rule stays in {@code @Size} so each failure gets its own message.
     */
    @ToString.Exclude
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
             message = "Password must contain at least one uppercase letter, one lowercase letter and one digit")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Company name is required")
    @Size(max = 100, message = "Company name must not exceed 100 characters")
    private String companyName;

    /**
     * Carried as a String, not the Role enum: an unknown value must fail as a 400
     * validation error with this message, whereas enum binding would fail earlier
     * in Jackson with an unhelpful deserialization error.
     */
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(ROLE_AUDITOR|ROLE_ADMIN)$",
             message = "Role must be ROLE_AUDITOR or ROLE_ADMIN")
    private String role;
}
