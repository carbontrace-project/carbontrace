package com.carbontrace.modules.auth.serviceimpl;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carbontrace.common.AppConstants;
import com.carbontrace.exception.BadRequestException;
import com.carbontrace.modules.auth.entity.OtpCode;
import com.carbontrace.modules.auth.entity.OtpPurpose;
import com.carbontrace.modules.auth.repository.OtpCodeRepository;
import com.carbontrace.modules.auth.service.OtpService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OTP lifecycle per COMMANDO.md Section 9.
 *
 * <p>The code value is never logged here. It is returned to the caller for
 * delivery, and {@code EmailServiceImpl}'s fallback path is the only place it may
 * appear in a log (COMMANDO.md Section 10).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final String NO_ACTIVE_CODE = "No active verification code. Please request a new code.";
    private static final String EXPIRED = "Verification code has expired. Please request a new code.";
    private static final String TOO_MANY_ATTEMPTS =
            "Too many failed attempts. This code is no longer valid — please request a new code.";
    private static final String INVALID_CODE = "Invalid verification code. ";

    /** Cryptographically strong, thread-safe, and seeded once (COMMANDO.md Section 9). */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /** 10^6, so nextInt yields the full 000000-999999 range. */
    private static final int OTP_UPPER_BOUND = 1_000_000;

    private final OtpCodeRepository otpCodeRepository;

    /**
     * {@inheritDoc}
     *
     * <p>Transactional because the invalidate-then-insert pair must not leave a
     * window where two codes are live, and because the bulk update is a
     * {@code @Modifying} query, which requires an active transaction.
     */
    @Override
    @Transactional
    public String generateOtp(String email, OtpPurpose purpose) {
        // Section 9: "Issuing a new OTP marks all previous unused OTPs for that
        // (email, purpose) as used" — so exactly one code is ever verifiable.
        int invalidated = otpCodeRepository.markAllAsUsed(email, purpose);
        if (invalidated > 0) {
            log.info("Invalidated {} previous unused {} code(s) for {}", invalidated, purpose, email);
        }

        String code = generateNumericCode();
        OtpCode otpCode = OtpCode.builder()
                .email(email)
                .code(code)
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(AppConstants.OTP_EXPIRY_MINUTES))
                .build();
        otpCodeRepository.save(otpCode);

        // The code itself is deliberately absent from this line.
        log.info("Issued {} code for {} (valid {} minutes)", purpose, email, AppConstants.OTP_EXPIRY_MINUTES);
        return code;
    }

    /**
     * {@inheritDoc}
     *
     * <p>{@code noRollbackFor = BadRequestException.class} is load-bearing, not
     * decoration. Every rejection path here throws that exception, and Spring
     * rolls back on any unchecked exception by default — which would discard the
     * attempt increment and the invalidation written moments earlier. The
     * Section 9 rule "maximum 5 verification attempts per OTP" would then never
     * take effect: the counter would reset to 0 on every failure, allowing
     * unlimited guesses against a 6-digit code. The write must survive the
     * exception that reports it.
     */
    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public void verifyOtp(String email, String code, OtpPurpose purpose) {
        OtpCode otpCode = otpCodeRepository
                .findFirstByEmailAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> {
                    log.warn("No active {} code for {}", purpose, email);
                    return new BadRequestException(NO_ACTIVE_CODE);
                });

        // Expiry is checked BEFORE the code comparison, and the row is left
        // unused: an expired code is already dead, and keeping it lets every
        // retry report "expired" rather than degrading to "no active code".
        if (otpCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Expired {} code presented for {}", purpose, email);
            throw new BadRequestException(EXPIRED);
        }

        if (!otpCode.getCode().equals(code)) {
            int attempts = otpCode.getAttempts() + 1;
            otpCode.setAttempts(attempts);

            // Section 9: "Maximum 5 verification attempts per OTP; after that the
            // code is invalidated and a new one must be requested."
            if (attempts >= AppConstants.OTP_MAX_ATTEMPTS) {
                otpCode.setIsUsed(true);
                otpCodeRepository.save(otpCode);
                log.warn("{} code for {} invalidated after {} failed attempts", purpose, email, attempts);
                throw new BadRequestException(TOO_MANY_ATTEMPTS);
            }

            otpCodeRepository.save(otpCode);
            int remaining = AppConstants.OTP_MAX_ATTEMPTS - attempts;
            log.warn("Invalid {} code for {} ({} attempt(s) remaining)", purpose, email, remaining);
            throw new BadRequestException(INVALID_CODE + remaining + " attempt(s) remaining.");
        }

        // Section 9: codes are single-use.
        otpCode.setIsUsed(true);
        otpCodeRepository.save(otpCode);
        log.info("Verified {} code for {}", purpose, email);
    }

    /**
     * @return a 6-digit numeric string, zero-padded so codes like {@code 000042}
     *         keep the full width the {@code VARCHAR(6)} column and the
     *         {@code ^\d{6}$} DTO pattern expect
     */
    private String generateNumericCode() {
        return String.format("%0" + AppConstants.OTP_LENGTH + "d", SECURE_RANDOM.nextInt(OTP_UPPER_BOUND));
    }
}
