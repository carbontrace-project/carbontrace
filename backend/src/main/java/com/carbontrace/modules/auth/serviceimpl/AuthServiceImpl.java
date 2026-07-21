package com.carbontrace.modules.auth.serviceimpl;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carbontrace.common.AppConstants;
import com.carbontrace.exception.BadRequestException;
import com.carbontrace.exception.ResourceNotFoundException;
import com.carbontrace.exception.UnauthorizedException;
import com.carbontrace.modules.auth.dto.AuthResponse;
import com.carbontrace.modules.auth.dto.ForgotPasswordRequest;
import com.carbontrace.modules.auth.dto.LoginRequest;
import com.carbontrace.modules.auth.dto.RefreshTokenRequest;
import com.carbontrace.modules.auth.dto.RegisterRequest;
import com.carbontrace.modules.auth.dto.ResendOtpRequest;
import com.carbontrace.modules.auth.dto.ResetPasswordRequest;
import com.carbontrace.modules.auth.dto.VerifyOtpRequest;
import com.carbontrace.modules.auth.entity.OtpPurpose;
import com.carbontrace.modules.auth.entity.RefreshToken;
import com.carbontrace.modules.auth.entity.Role;
import com.carbontrace.modules.auth.entity.User;
import com.carbontrace.modules.auth.repository.RefreshTokenRepository;
import com.carbontrace.modules.auth.repository.UserRepository;
import com.carbontrace.modules.auth.service.AuthService;
import com.carbontrace.modules.auth.service.EmailService;
import com.carbontrace.modules.auth.service.OtpService;
import com.carbontrace.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The seven Section 8.1 auth flows.
 *
 * <p>Passwords, OTP codes and token values are never logged (COMMANDO.md
 * Section 21) — log lines carry the email and the outcome only.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String EMAIL_ALREADY_REGISTERED = "An account with this email already exists";
    private static final String EMAIL_NOT_VERIFIED = "Email not verified";
    private static final String INVALID_CREDENTIALS = "Invalid email or password";
    private static final String ACCOUNT_DEACTIVATED = "This account has been deactivated";
    private static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    private static final String EXPIRED_REFRESH_TOKEN = "Refresh token has expired. Please log in again.";
    private static final String REVOKED_REFRESH_TOKEN = "Refresh token has been revoked. Please log in again.";
    private static final String WRONG_VERIFY_PURPOSE =
            "Password reset codes are redeemed by the reset-password endpoint, not here.";
    private static final String USER_NOT_FOUND = "User not found with email: ";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Field-injected for the same reason as {@code EmailServiceImpl}: Lombok does
     * not copy {@code @Value} onto {@code @RequiredArgsConstructor} parameters, so
     * a final field would arrive as 0.
     */
    @Value("${app.jwt.refresh-expiration}")
    private long refreshTokenExpirationMs;

    // ---------------------------------------------------------------- register

    @Override
    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        // Explicit pre-check gives a clear 400 instead of the database's opaque
        // 409. The unique constraint on users.email remains the real guarantee —
        // it still wins a race between two concurrent registrations, surfacing as
        // DataIntegrityViolationException -> 409 (COMMANDO.md Section 23).
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration rejected — email already registered: {}", request.getEmail());
            throw new BadRequestException(EMAIL_ALREADY_REGISTERED);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .companyName(request.getCompanyName())
                .role(Role.valueOf(request.getRole()))
                .isEmailVerified(false)
                .isActive(true)
                .build();
        userRepository.save(user);
        log.info("Registered {} ({}) — awaiting email verification", user.getEmail(), user.getRole());

        deliverOtp(user.getEmail(), OtpPurpose.REGISTRATION);

        return Map.of(
                "email", user.getEmail(),
                "otpExpiresInMinutes", AppConstants.OTP_EXPIRY_MINUTES);
    }

    // --------------------------------------------------------------- verifyOtp

    /**
     * {@inheritDoc}
     *
     * <p>{@code noRollbackFor} is required here, not only on
     * {@code OtpServiceImpl.verifyOtp}. That call joins THIS transaction
     * (propagation REQUIRED), so the inner method's own rollback rules do not
     * govern the outcome — these do. Without it, a wrong code rolls back the
     * attempt increment the OTP service just wrote, and the Section 9 five-attempt
     * limit silently stops working when reached through this flow.
     */
    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        OtpPurpose purpose = OtpPurpose.valueOf(request.getPurpose());

        // A PASSWORD_RESET code is single-use and belongs to resetPassword.
        // Consuming it here would burn the code and leave the user unable to
        // complete the reset, so it is refused with a message that says where to
        // send it instead.
        if (purpose != OtpPurpose.REGISTRATION) {
            throw new BadRequestException(WRONG_VERIFY_PURPOSE);
        }

        // Code FIRST, user second. Loading the user first would answer an unknown
        // address with 404 "User not found" while a real one gets 400 — turning
        // this endpoint into an account-existence oracle and undoing the
        // protection forgot-password implements (COMMANDO.md Section 9). With
        // this order an address that was never registered has no live code, so it
        // gets the same generic 400 as any other bad attempt.
        otpService.verifyOtp(request.getEmail(), request.getCode(), purpose);
        User user = findUser(request.getEmail());

        user.setIsEmailVerified(true);
        userRepository.save(user);
        log.info("Email verified for {}", user.getEmail());

        // Section 8.1: verify-otp returns tokens — the user is logged in already.
        return issueTokens(user);
    }

    // --------------------------------------------------------------- resendOtp

    @Override
    @Transactional
    public void resendOtp(ResendOtpRequest request) {
        OtpPurpose purpose = OtpPurpose.valueOf(request.getPurpose());
        // Same silence as forgot-password: resend must not reveal whether the
        // address is registered (COMMANDO.md Section 9).
        userRepository.findByEmail(request.getEmail())
                .ifPresentOrElse(
                        user -> deliverOtp(user.getEmail(), purpose),
                        () -> log.warn("Resend requested for an unknown email: {}", request.getEmail()));
    }

    // ------------------------------------------------------------------- login

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Credentials are checked FIRST. Reporting "Email not verified" before
        // the password is validated would tell an attacker the address exists.
        authenticate(request.getEmail(), request.getPassword());

        User user = findUser(request.getEmail());
        if (!Boolean.TRUE.equals(user.getIsEmailVerified())) {
            log.warn("Login blocked — email not verified: {}", user.getEmail());
            throw new BadRequestException(EMAIL_NOT_VERIFIED);
        }

        log.info("Login successful for {}", user.getEmail());
        return issueTokens(user);
    }

    // ----------------------------------------------------------------- refresh

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> {
                    log.warn("Refresh rejected — token not recognised");
                    return new UnauthorizedException(INVALID_REFRESH_TOKEN);
                });

        // Both rejections happen BEFORE anything is written, so there is no state
        // change for the thrown exception to roll back.
        if (Boolean.TRUE.equals(stored.getIsRevoked())) {
            log.warn("Refresh rejected — token already revoked for {}", stored.getUser().getEmail());
            throw new UnauthorizedException(REVOKED_REFRESH_TOKEN);
        }
        if (stored.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Refresh rejected — token expired for {}", stored.getUser().getEmail());
            throw new UnauthorizedException(EXPIRED_REFRESH_TOKEN);
        }

        // Rotation (COMMANDO.md Section 10): the presented token dies here, in the
        // same transaction that issues its replacement.
        stored.setIsRevoked(true);
        refreshTokenRepository.save(stored);

        User user = stored.getUser();
        log.info("Refresh token rotated for {}", user.getEmail());
        return issueTokens(user);
    }

    // ---------------------------------------------------------- forgotPassword

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Section 9: always the same outcome, so the caller's generic message is
        // truthful whether or not the address exists.
        userRepository.findByEmail(request.getEmail())
                .ifPresentOrElse(
                        user -> deliverOtp(user.getEmail(), OtpPurpose.PASSWORD_RESET),
                        () -> log.warn("Password reset requested for an unknown email: {}", request.getEmail()));
    }

    // ----------------------------------------------------------- resetPassword

    /**
     * {@inheritDoc}
     *
     * <p>{@code noRollbackFor} for the same reason as {@link #verifyOtp}: the OTP
     * check runs inside this transaction, so a wrong reset code must still leave
     * its attempt increment committed.
     */
    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public void resetPassword(ResetPasswordRequest request) {
        // Code FIRST, user second — same reasoning as verifyOtp. This endpoint is
        // the one that most directly undermines forgot-password's guarantee: an
        // attacker can call forgot-password for any address and then read the
        // difference between 404 and 400 here.
        otpService.verifyOtp(request.getEmail(), request.getCode(), OtpPurpose.PASSWORD_RESET);
        User user = findUser(request.getEmail());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Every existing session dies with the old password. Without this, a
        // stolen refresh token would survive the reset for its full 7 days, and
        // the reset would not actually lock the attacker out.
        int revoked = refreshTokenRepository.revokeAllByUser(user);
        log.info("Password reset for {} — {} refresh token(s) revoked", user.getEmail(), revoked);
    }

    // ----------------------------------------------------------------- helpers

    /**
     * Runs the credential check through the {@code AuthenticationManager}, which
     * is backed by {@code CustomUserDetailsService} (STEP A009).
     *
     * <p>Spring's authentication exceptions are translated here because
     * {@code GlobalExceptionHandler} has no mapping for them — left alone they
     * would surface as 500 via the catch-all (COMMANDO.md Section 23).
     */
    private void authenticate(String email, String rawPassword) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, rawPassword));
        } catch (DisabledException ex) {
            // is_active = false (mapped by CustomUserDetailsService).
            log.warn("Login blocked — account deactivated: {}", email);
            throw new UnauthorizedException(ACCOUNT_DEACTIVATED);
        } catch (BadCredentialsException ex) {
            // Covers BOTH a wrong password and an unknown email: the provider
            // hides UsernameNotFoundException behind BadCredentialsException, and
            // this single message preserves that (Section 9: no enumeration).
            log.warn("Login failed — invalid credentials for {}", email);
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }
    }

    /** Issues a JWT access token plus a freshly persisted refresh token. */
    private AuthResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getEmail(), user.getId(), user.getRole().name());

        // Opaque and random: a refresh token carries no claims, so nothing can be
        // read out of it and only the stored row makes it valid. 36 chars, well
        // inside the VARCHAR(500) column.
        String refreshTokenValue = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiryDate(LocalDateTime.now().plusNanos(refreshTokenExpirationMs * 1_000_000))
                .isRevoked(false)
                .build());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    /** Generates a code and hands it to delivery; the code is never logged here. */
    private void deliverOtp(String email, OtpPurpose purpose) {
        String code = otpService.generateOtp(email, purpose);
        emailService.sendOtpEmail(email, code, purpose);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND + email));
    }
}
