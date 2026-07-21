package com.carbontrace.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The {@code data} payload returned by verify-otp, login and refresh
 * (COMMANDO.md Section 8.1). Field names and order match that JSON exactly.
 *
 * <p>This is a response DTO, so it carries no validation annotations — it is
 * never bound from a request body.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    /** Excluded from {@code toString} (COMMANDO.md Section 21). */
    @ToString.Exclude
    private String accessToken;

    /** Excluded from {@code toString} (COMMANDO.md Section 21). */
    @ToString.Exclude
    private String refreshToken;

    /**
     * Always the literal {@code "Bearer"} per the Section 8.1 payload.
     *
     * <p>Deliberately NOT {@code AppConstants.TOKEN_PREFIX}, which is
     * {@code "Bearer "} WITH a trailing space because it is used to strip the
     * Authorization header. Reusing it here would emit {@code "Bearer "} in the
     * JSON and break the contract.
     */
    @Builder.Default
    private String tokenType = "Bearer";

    private Long userId;

    private String email;

    /** {@code ROLE_AUDITOR} or {@code ROLE_ADMIN} — the Role enum's name. */
    private String role;

    private String firstName;

    private String lastName;
}
