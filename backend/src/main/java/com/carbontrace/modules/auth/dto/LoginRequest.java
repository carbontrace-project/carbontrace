package com.carbontrace.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Request body of {@code POST /api/auth/login} (COMMANDO.md Section 8.1).
 *
 * <p>The password carries {@code @NotBlank} ONLY — deliberately not the
 * registration strength rules. Login must verify the credential that was
 * actually stored; re-applying the policy here would reject valid accounts
 * whenever the policy changes, and would tell an attacker the exact password
 * shape before any credential check runs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    /** Excluded from {@code toString} (COMMANDO.md Section 21). */
    @ToString.Exclude
    @NotBlank(message = "Password is required")
    private String password;
}
