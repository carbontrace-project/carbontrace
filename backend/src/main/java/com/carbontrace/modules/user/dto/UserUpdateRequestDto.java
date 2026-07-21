package com.carbontrace.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body of {@code PUT /api/users/me} (COMMANDO.md Section 8.2).
 *
 * <p><strong>This class is the primary enforcement of the Section 8.2 rule that
 * the endpoint updates "firstName, lastName, companyName only (NOT email, NOT
 * role, NOT password)".</strong> Those three fields do not exist here, so a body
 * containing them cannot bind them onto anything — the values are silently
 * discarded by Jackson before any application code runs. {@code UserMapper}
 * ignores the same targets explicitly as a second, independent barrier.
 *
 * <p>Naming: Section 6 lists only {@code dto/UserResponseDto} for this module and
 * never names a request DTO, so the name is chosen here. It is deliberately NOT
 * {@code UserRequestDto} — the {@code {Module}RequestDto} convention used by the
 * other modules denotes a create-or-update body, and users are created solely by
 * {@code POST /api/auth/register}. "Update" states the endpoint's whole scope.
 *
 * <p>Validation mirrors {@code RegisterRequest}: the same three fields, the same
 * messages, the same Section 7 column caps. Anything a user could set at
 * registration they can set here, and nothing more.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequestDto {

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Company name is required")
    @Size(max = 100, message = "Company name must not exceed 100 characters")
    private String companyName;
}
