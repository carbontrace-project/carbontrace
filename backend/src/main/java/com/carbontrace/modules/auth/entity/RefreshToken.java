package com.carbontrace.modules.auth.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * A refresh token issued to a user — maps to {@code refresh_tokens}
 * (COMMANDO.md Section 7).
 *
 * <p>Rotation (COMMANDO.md Section 8.1) revokes the presented token and stores a
 * new row rather than mutating the token value, so revoked rows stay auditable.
 *
 * <p>The {@code user} association is excluded from {@code toString}/
 * {@code equals} because {@code @Data} would otherwise trigger the lazy proxy —
 * and potentially log user data — every time a token is printed.
 */
@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The opaque token value handed to the client. Excluded from
     * {@code toString} so it can never reach a log line
     * (COMMANDO.md Section 21).
     */
    @ToString.Exclude
    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Builder.Default
    @ColumnDefault("false")
    @Column(name = "is_revoked")
    private Boolean isRevoked = Boolean.FALSE;

    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
