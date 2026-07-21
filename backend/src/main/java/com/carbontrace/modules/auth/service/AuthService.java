package com.carbontrace.modules.auth.service;

import java.util.Map;

import com.carbontrace.modules.auth.dto.AuthResponse;
import com.carbontrace.modules.auth.dto.ForgotPasswordRequest;
import com.carbontrace.modules.auth.dto.LoginRequest;
import com.carbontrace.modules.auth.dto.RefreshTokenRequest;
import com.carbontrace.modules.auth.dto.RegisterRequest;
import com.carbontrace.modules.auth.dto.ResendOtpRequest;
import com.carbontrace.modules.auth.dto.ResetPasswordRequest;
import com.carbontrace.modules.auth.dto.VerifyOtpRequest;

/**
 * The seven authentication flows of COMMANDO.md Section 8.1.
 */
public interface AuthService {

    /**
     * Creates an unverified account and sends a REGISTRATION code.
     *
     * @return the Section 8.1 register payload:
     *         {@code { "email": ..., "otpExpiresInMinutes": 10 }}. A Map rather
     *         than a DTO because Section 6 defines exactly eight auth DTOs and
     *         none of them covers this payload — changing that is a Section 25.2
     *         contract decision, not an implementation one.
     */
    Map<String, Object> register(RegisterRequest request);

    /** Verifies a REGISTRATION code, marks the email verified, and auto-logs in. */
    AuthResponse verifyOtp(VerifyOtpRequest request);

    /** Issues a fresh code, invalidating any previous unused one for that purpose. */
    void resendOtp(ResendOtpRequest request);

    /** Authenticates by password and issues tokens. */
    AuthResponse login(LoginRequest request);

    /** Rotates a refresh token: the presented token is revoked and a new pair issued. */
    AuthResponse refresh(RefreshTokenRequest request);

    /**
     * Sends a PASSWORD_RESET code if the account exists.
     *
     * <p>Behaves identically for unknown addresses — the caller must respond with
     * the same generic message either way (COMMANDO.md Section 9: no account
     * enumeration).
     */
    void forgotPassword(ForgotPasswordRequest request);

    /** Consumes a PASSWORD_RESET code and stores the new password. */
    void resetPassword(ResetPasswordRequest request);
}
