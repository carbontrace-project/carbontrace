package com.carbontrace.modules.auth.serviceimpl;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.carbontrace.common.AppConstants;
import com.carbontrace.modules.auth.entity.OtpPurpose;
import com.carbontrace.modules.auth.service.EmailService;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Gmail SMTP delivery with a console fallback
 * (COMMANDO.md Section 18 Flow 8).
 *
 * <p>This class NEVER throws. A mail failure must not break registration or
 * password reset — the caller carries on and the user can use "Resend OTP".
 *
 * <p>{@link #logOtpFallback(String, String)} is the ONE place in the codebase
 * where an OTP code may be written to a log (COMMANDO.md Section 10). It exists
 * so the app is usable before MANUAL SETUP B (the Gmail app password) is done.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final String REGISTRATION_SUBJECT = "Your CarbonTrace verification code";
    private static final String PASSWORD_RESET_SUBJECT = "Your CarbonTrace password reset code";

    private final JavaMailSender mailSender;

    /**
     * When true, mail is skipped entirely and the code goes to the log.
     * Defaults to true (COMMANDO.md Section 19) so the app runs without SMTP.
     *
     * <p>Injected on the field rather than the constructor: {@code @Value} is not
     * among the annotations Lombok copies onto {@code @RequiredArgsConstructor}
     * parameters, so a final field here would arrive null. This is property
     * injection, not the {@code @Autowired} bean-field injection Section 21
     * forbids — the same approach {@code AiConfig} already uses.
     */
    @Value("${app.mail.log-otp-fallback:true}")
    private boolean logOtpFallbackEnabled;

    /** Empty until MANUAL SETUP B is done; used as the From address. */
    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Override
    public void sendOtpEmail(String email, String code, OtpPurpose purpose) {
        if (logOtpFallbackEnabled) {
            log.info("SMTP delivery disabled (app.mail.log-otp-fallback=true) — logging the code instead");
            logOtpFallback(email, code);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            if (StringUtils.hasText(fromAddress)) {
                helper.setFrom(fromAddress);
            }
            helper.setTo(email);
            helper.setSubject(subjectFor(purpose));
            helper.setText(buildHtmlBody(code, purpose), true);
            mailSender.send(message);
            log.info("Sent {} code to {}", purpose, email);
        } catch (Exception ex) {
            // Deliberately broad: MailException, MessagingException and any
            // JavaMail runtime failure must all degrade to the fallback rather
            // than propagate (Section 18 Flow 8). The exception is logged with
            // its stack trace; the code is not in that message.
            log.error("Failed to send {} code to {} — falling back to the log", purpose, email, ex);
            logOtpFallback(email, code);
        }
    }

    /**
     * The ONLY sanctioned place an OTP code is logged (COMMANDO.md Section 10).
     * WARN level, because a code in the log is a deliberate development
     * concession and should stand out in production output.
     */
    private void logOtpFallback(String email, String code) {
        log.warn("OTP for {}: {}", email, code);
    }

    private String subjectFor(OtpPurpose purpose) {
        return purpose == OtpPurpose.PASSWORD_RESET ? PASSWORD_RESET_SUBJECT : REGISTRATION_SUBJECT;
    }

    /**
     * Simple HTML: the code, what it is for, and the expiry note
     * (COMMANDO.md Section 18 Flow 8).
     */
    private String buildHtmlBody(String code, OtpPurpose purpose) {
        String intro = purpose == OtpPurpose.PASSWORD_RESET
                ? "Use this code to reset your CarbonTrace password."
                : "Use this code to verify your CarbonTrace account.";
        return """
                <div style="font-family: Arial, sans-serif; color: #1a1a1a;">
                  <h2 style="margin-bottom: 4px;">CarbonTrace</h2>
                  <p>%s</p>
                  <p style="font-size: 28px; font-weight: bold; letter-spacing: 4px;">%s</p>
                  <p>This code expires in %d minutes and can be used once.</p>
                  <p style="color: #666; font-size: 12px;">
                    If you did not request this code, you can safely ignore this email.
                  </p>
                </div>
                """.formatted(intro, code, AppConstants.OTP_EXPIRY_MINUTES);
    }
}
