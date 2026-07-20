package com.carbontrace.modules.auth.entity;

/**
 * The two application roles (COMMANDO.md Section 3).
 *
 * <p>The constant names are the literal values stored in {@code users.role} and
 * carried in the JWT {@code role} claim, so they match
 * {@code AppConstants.ROLE_AUDITOR} / {@code AppConstants.ROLE_ADMIN} exactly.
 */
public enum Role {
    ROLE_AUDITOR,
    ROLE_ADMIN
}
