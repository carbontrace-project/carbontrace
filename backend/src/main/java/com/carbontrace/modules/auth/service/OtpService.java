package com.carbontrace.modules.auth.service;

import com.carbontrace.modules.auth.entity.OtpPurpose;

/**
 * Issues and verifies one-time codes (COMMANDO.md Section 9 OTP rules).
 *
 * <p>Codes are scoped per (email, purpose): a REGISTRATION code and a
 * PASSWORD_RESET code for the same address are independent.
 */
public interface OtpService {

    /**
     * Issues a fresh code, invalidating every previous unused code for the same
     * (email, purpose) so only one code is ever live.
     *
     * @return the 6-digit code, for delivery by
     *         {@link EmailService#sendOtpEmail}. The caller must NOT log it.
     */
    String generateOtp(String email, OtpPurpose purpose);

    /**
     * Consumes the live code for (email, purpose).
     *
     * <p>On success the code is marked used and can never verify again. On a
     * wrong code the attempt counter increases, and the code is invalidated once
     * the attempt limit is reached.
     *
     * @throws com.carbontrace.exception.BadRequestException when no live code
     *         exists, the code has expired, the attempt limit is reached, or the
     *         code does not match
     */
    void verifyOtp(String email, String code, OtpPurpose purpose);
}
