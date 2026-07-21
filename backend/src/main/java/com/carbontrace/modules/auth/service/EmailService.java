package com.carbontrace.modules.auth.service;

import com.carbontrace.modules.auth.entity.OtpPurpose;

/**
 * Delivers OTP emails (COMMANDO.md Section 18 Flow 8).
 */
public interface EmailService {

    /**
     * Sends the one-time code to the user.
     *
     * <p>Never throws: a mail failure must not break registration or
     * password-reset (COMMANDO.md Section 18 Flow 8). When delivery is
     * unavailable the code is written to the application log instead, which is
     * the ONLY place an OTP code may ever be logged (Section 10).
     */
    void sendOtpEmail(String email, String code, OtpPurpose purpose);
}
