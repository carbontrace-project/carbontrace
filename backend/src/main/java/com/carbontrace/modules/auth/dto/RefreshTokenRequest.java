package com.carbontrace.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Request body of {@code POST /api/auth/refresh} (COMMANDO.md Section 8.1).
 *
 * <p>The token is opaque to this layer: only presence and the Section 7 column
 * width are checked here. Validity, expiry and revocation are database facts
 * resolved by AuthService at STEP A011, which rotates the token on success.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {

    /** Excluded from {@code toString} (COMMANDO.md Section 21). */
    @ToString.Exclude
    @NotBlank(message = "Refresh token is required")
    @Size(max = 500, message = "Refresh token must not exceed 500 characters")
    private String refreshToken;
}
