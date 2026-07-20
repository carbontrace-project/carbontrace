package com.carbontrace.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.carbontrace.common.ApiResponse;
import com.carbontrace.common.AppConstants;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Stateless JWT security chain implementing the COMMANDO.md Section 10 matrix.
 *
 * <p>Authorities are the raw role strings from the token claim
 * ({@code ROLE_AUDITOR} / {@code ROLE_ADMIN}), so {@code hasAuthority} is used
 * with {@link AppConstants} rather than {@code hasRole}, which would add a
 * second {@code ROLE_} prefix.
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ACCESS_DENIED_MESSAGE = "Access denied";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthEntryPoint authEntryPoint;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(this::writeAccessDenied))
                .authorizeHttpRequests(auth -> auth
                        // Permit all
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/marketplace/**").permitAll()
                        // Require ROLE_ADMIN
                        .requestMatchers("/api/admin/**").hasAuthority(AppConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/emission-factors/**").hasAuthority(AppConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/emission-factors/**").hasAuthority(AppConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/vendors/*").hasAuthority(AppConstants.ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/vendors/*/toggle-active").hasAuthority(AppConstants.ROLE_ADMIN)
                        // Everything else requires authentication
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Writes 403 in the {@link ApiResponse} error format (COMMANDO.md Sections 10
     * and 23) when an authenticated caller lacks the required role.
     *
     * <p>The response is written directly rather than via
     * {@code response.sendError()}: sendError triggers an ERROR dispatch to
     * {@code /error}, which re-enters this filter chain WITHOUT the JWT filter
     * ({@code OncePerRequestFilter} skips ERROR dispatches), so the request would
     * look unauthenticated and {@link AuthEntryPoint} would replace the 403 with
     * a 401.
     */
    private void writeAccessDenied(HttpServletRequest request,
                                   HttpServletResponse response,
                                   org.springframework.security.access.AccessDeniedException ex) throws IOException {
        // Path and method only — never the Authorization header or its token.
        log.warn("Access denied: {} {}", request.getMethod(), request.getRequestURI());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(ACCESS_DENIED_MESSAGE));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
