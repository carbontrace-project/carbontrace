package com.carbontrace.modules.user.service;

import com.carbontrace.modules.user.dto.UserResponseDto;
import com.carbontrace.modules.user.dto.UserUpdateRequestDto;

/**
 * The two Section 8.2 profile operations, both scoped to the caller's own
 * account.
 *
 * <p>The email is passed in by the controller, which reads it from the
 * authenticated principal. No method here takes an id: an endpoint that accepted
 * one would let any authenticated caller read or edit another user's profile,
 * and Section 10 grants no such permission to {@code ROLE_AUDITOR}.
 */
public interface UserService {

    /**
     * @param email the authenticated user's email (the JWT subject)
     * @return the caller's profile
     * @throws com.carbontrace.exception.ResourceNotFoundException if no user has that email
     */
    UserResponseDto getCurrentUser(String email);

    /**
     * Updates the caller's first name, last name and company name. No other
     * column is writable through this path.
     *
     * @param email   the authenticated user's email (the JWT subject)
     * @param request the three editable fields
     * @return the profile as stored after the update
     * @throws com.carbontrace.exception.ResourceNotFoundException if no user has that email
     */
    UserResponseDto updateCurrentUser(String email, UserUpdateRequestDto request);
}
