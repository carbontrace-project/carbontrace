package com.carbontrace.modules.auth.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * A one-time verification code — maps to {@code otp_codes}
 * (COMMANDO.md Section 7).
 *
 * <p>Standalone table: codes are keyed by {@code email}, not by a user foreign
 * key, so a code can be issued before the user row exists and can survive an
 * email that never completes registration.
 *
 * <p>Lifecycle per COMMANDO.md Section 9: 6 digits, 10-minute expiry, single-use,
 * at most 5 verification attempts, and issuing a new code marks every previous
 * unused code for the same (email, purpose) as used.
 */
@Entity
@Table(name = "otp_codes", indexes = {
        @Index(name = "idx_otp_email_purpose", columnList = "email, purpose")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /**
     * The 6-digit code. Excluded from {@code toString} so logging an OtpCode can
     * never print it; the console-fallback delivery path (STEP A010) logs the
     * code deliberately and explicitly, never via this entity
     * (COMMANDO.md Section 21).
     */
    @ToString.Exclude
    @Column(name = "code", nullable = false, length = 6)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 20)
    private OtpPurpose purpose;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    @ColumnDefault("0")
    @Column(name = "attempts")
    private Integer attempts = 0;

    @Builder.Default
    @ColumnDefault("false")
    @Column(name = "is_used")
    private Boolean isUsed = Boolean.FALSE;

    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
