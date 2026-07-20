package com.carbontrace.modules.auth.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Application user — maps to the {@code users} table (COMMANDO.md Section 7).
 *
 * <p>The boolean flags are {@link Boolean} rather than {@code boolean} on
 * purpose: Hibernate renders primitives as {@code NOT NULL}, which would
 * deviate from the Section 7 DDL ({@code BOOLEAN DEFAULT ...}). Defaults are
 * declared twice — {@link ColumnDefault} for the database DDL and
 * {@link Builder.Default} for objects built in Java.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * BCrypt hash — never the plaintext password. Excluded from {@code toString}
     * so logging a User can never print the hash (COMMANDO.md Section 21).
     */
    @ToString.Exclude
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    /** Set true only after successful OTP verification; login is rejected until then. */
    @Builder.Default
    @ColumnDefault("false")
    @Column(name = "is_email_verified")
    private Boolean isEmailVerified = Boolean.FALSE;

    /** Soft-deactivation flag toggled by admins; users are never deleted. */
    @Builder.Default
    @ColumnDefault("true")
    @Column(name = "is_active")
    private Boolean isActive = Boolean.TRUE;

    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
