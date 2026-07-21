package com.carbontrace.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.carbontrace.modules.auth.entity.User;
import com.carbontrace.modules.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Loads users for Spring Security from the {@code users} table
 * (COMMANDO.md Section 10).
 *
 * <p>Email is the login identifier, so it serves as the UserDetails username.
 *
 * <p>Wiring: this is the only {@link UserDetailsService} bean in the context, so
 * Spring Security's {@code InitializeUserDetailsBeanManagerConfigurer} pairs it
 * with the {@code PasswordEncoder} bean from {@link SecurityConfig} to build the
 * {@code DaoAuthenticationProvider} behind the {@code AuthenticationManager}
 * exposed there. No change to SecurityConfig is required — an explicit provider
 * would only duplicate what that configurer already does.
 *
 * <p>This class does NOT participate in JWT request authentication:
 * {@link JwtAuthenticationFilter} builds its authorities from the token's
 * {@code role} claim and never hits the database. This service is used on the
 * password-login path only (AuthService, STEP A011).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final String USER_NOT_FOUND = "User not found with email: ";

    private final UserRepository userRepository;

    /**
     * @param email the login identifier (the UserDetails "username")
     * @throws UsernameNotFoundException when no user has that email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    // DaoAuthenticationProvider hides this exception behind
                    // BadCredentialsException by default, so the client cannot
                    // tell "unknown email" from "wrong password" — no account
                    // enumeration (COMMANDO.md Section 9).
                    log.warn("Authentication attempt for unknown email: {}", email);
                    return new UsernameNotFoundException(USER_NOT_FOUND + email);
                });

        // Single authority, taken verbatim from the role enum: the stored value
        // already carries the ROLE_ prefix, matching the JWT "role" claim and the
        // hasAuthority(...) checks in SecurityConfig's Section 10 matrix.
        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(user.getRole().name()));

        // is_active maps to "enabled": a deactivated user fails authentication
        // with DisabledException. Fail-closed on null — the column is nullable in
        // the Section 7 DDL, so a row without the default is treated as inactive
        // rather than silently granted access.
        boolean enabled = Boolean.TRUE.equals(user.getIsActive());

        // is_email_verified is deliberately NOT consulted here. Section 9 requires
        // login to be rejected with a clear "Email not verified" message, which is
        // an AuthService rule (STEP A011) returning 400. Mapping it onto "enabled"
        // would collapse it into the same generic DisabledException as
        // deactivation and lose that distinction.
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(!enabled)
                .accountLocked(false)
                .accountExpired(false)
                .credentialsExpired(false)
                .build();
    }
}
