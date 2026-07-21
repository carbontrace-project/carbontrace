package com.carbontrace.modules.user.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body of {@code GET} and {@code PUT /api/users/me}
 * (COMMANDO.md Section 8.2 — "the current user's basic info").
 *
 * <p>Section 8.2 does not print an example, so the shape is the {@code users}
 * columns of Section 7 that the owner may see, minus {@code password}: a hash is
 * never sent over the wire, and omitting the field entirely (rather than nulling
 * it) means no future change can accidentally populate it.
 *
 * <p>{@code role} is a String, not the {@link com.carbontrace.modules.auth.entity.Role}
 * enum, matching how {@code AuthResponse} already serialises it — the client sees
 * {@code "ROLE_AUDITOR"} from both endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String companyName;
    private String role;
    private Boolean isEmailVerified;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
