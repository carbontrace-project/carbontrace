package com.carbontrace.modules.user.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carbontrace.common.ApiResponse;
import com.carbontrace.modules.user.dto.UserResponseDto;
import com.carbontrace.modules.user.dto.UserUpdateRequestDto;
import com.carbontrace.modules.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The two Section 8.2 profile endpoints, both open to any authenticated role.
 *
 * <p>Authorization needs no annotation: neither route matches a Section 10
 * permitAll or ROLE_ADMIN rule, so {@code SecurityConfig}'s
 * {@code anyRequest().authenticated()} covers them. A missing or invalid token
 * produces 401 from {@code AuthEntryPoint} before this class is reached.
 *
 * <p>The principal is the email string that {@code JwtAuthenticationFilter} put
 * in the SecurityContext from the token's subject. Taking it from there — rather
 * than from a path variable or the request body — is what makes "me" mean the
 * caller and nobody else.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private static final String GET_ME_MESSAGE = "Profile retrieved";
    private static final String UPDATE_ME_MESSAGE = "Profile updated successfully";

    private final UserService userService;

    /** 200 — the caller's own profile. */
    @GetMapping("/me")
    public ApiResponse<UserResponseDto> getCurrentUser(@AuthenticationPrincipal String email) {
        log.info("Profile requested for {}", email);
        return ApiResponse.success(GET_ME_MESSAGE, userService.getCurrentUser(email));
    }

    /** 200 — updates first name, last name and company name; nothing else. */
    @PutMapping("/me")
    public ApiResponse<UserResponseDto> updateCurrentUser(@AuthenticationPrincipal String email,
                                                          @Valid @RequestBody UserUpdateRequestDto request) {
        log.info("Profile update requested for {}", email);
        return ApiResponse.success(UPDATE_ME_MESSAGE, userService.updateCurrentUser(email, request));
    }
}
