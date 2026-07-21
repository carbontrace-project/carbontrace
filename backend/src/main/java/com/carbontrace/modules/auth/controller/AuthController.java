package com.carbontrace.modules.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.carbontrace.common.ApiResponse;
import com.carbontrace.modules.auth.dto.AuthResponse;
import com.carbontrace.modules.auth.dto.ForgotPasswordRequest;
import com.carbontrace.modules.auth.dto.LoginRequest;
import com.carbontrace.modules.auth.dto.RefreshTokenRequest;
import com.carbontrace.modules.auth.dto.RegisterRequest;
import com.carbontrace.modules.auth.dto.ResendOtpRequest;
import com.carbontrace.modules.auth.dto.ResetPasswordRequest;
import com.carbontrace.modules.auth.dto.VerifyOtpRequest;
import com.carbontrace.modules.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The seven public auth endpoints of COMMANDO.md Section 8.1.
 *
 * <p>All of {@code /api/auth/**} is permitAll in the Section 10 filter chain —
 * these routes must work before a token exists.
 *
 * <p>This class only validates, delegates and wraps. Every business rule lives in
 * {@code AuthService}; every failure becomes an {@code ApiResponse} error through
 * {@code GlobalExceptionHandler} (Section 23), so there is no try/catch here.
 *
 * <p>Request bodies are never logged: they carry passwords and OTP codes
 * (Section 21).
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // Verbatim from the COMMANDO.md Section 8.1 examples.
    private static final String REGISTER_MESSAGE =
            "Registration successful. An OTP has been sent to your email.";
    private static final String VERIFY_OTP_MESSAGE = "Email verified successfully";

    // Section 8.1 does not quote a message for the remaining five endpoints, so
    // these follow its tone. The forgot-password and resend-otp wording is
    // deliberately conditional ("If an account exists") — the response is
    // identical for unknown addresses, and a definite "sent" would be a lie that
    // also confirms the account exists (Section 9: no account enumeration).
    private static final String RESEND_OTP_MESSAGE =
            "If an account exists for that email, a new verification code has been sent.";
    private static final String LOGIN_MESSAGE = "Login successful";
    private static final String REFRESH_MESSAGE = "Token refreshed";
    private static final String FORGOT_PASSWORD_MESSAGE =
            "If an account exists for that email, a password reset code has been sent.";
    private static final String RESET_PASSWORD_MESSAGE = "Password reset successfully";

    private final AuthService authService;

    /** 201 — creates the account and sends a REGISTRATION code. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration requested for {}", request.getEmail());
        return ApiResponse.success(REGISTER_MESSAGE, authService.register(request));
    }

    /** 200 — verifies the code, marks the email verified, and returns tokens. */
    @PostMapping("/verify-otp")
    public ApiResponse<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("OTP verification requested for {}", request.getEmail());
        return ApiResponse.success(VERIFY_OTP_MESSAGE, authService.verifyOtp(request));
    }

    /** 200 — issues a fresh code, invalidating any previous unused one. */
    @PostMapping("/resend-otp")
    public ApiResponse<Void> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        log.info("OTP resend requested for {}", request.getEmail());
        authService.resendOtp(request);
        return ApiResponse.success(RESEND_OTP_MESSAGE, null);
    }

    /** 200 — authenticates and returns tokens. */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login requested for {}", request.getEmail());
        return ApiResponse.success(LOGIN_MESSAGE, authService.login(request));
    }

    /** 200 — rotates the refresh token and returns a new pair. */
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        // The token value itself is never logged (Section 21).
        log.info("Token refresh requested");
        return ApiResponse.success(REFRESH_MESSAGE, authService.refresh(request));
    }

    /** 200 — always, whether or not the account exists (Section 9). */
    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Password reset requested for {}", request.getEmail());
        authService.forgotPassword(request);
        return ApiResponse.success(FORGOT_PASSWORD_MESSAGE, null);
    }

    /** 200 — consumes the PASSWORD_RESET code and stores the new password. */
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Password reset submission for {}", request.getEmail());
        authService.resetPassword(request);
        return ApiResponse.success(RESET_PASSWORD_MESSAGE, null);
    }
}
