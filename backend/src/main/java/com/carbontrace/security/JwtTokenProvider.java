package com.carbontrace.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * Issues and parses HS256 access tokens (COMMANDO.md Section 10).
 *
 * <p>Token payload is exactly the Section 10 structure:
 * {@code sub} (email), {@code userId}, {@code role}, {@code iat}, {@code exp}.
 *
 * <p>Token strings and the signing secret are NEVER logged — rejection is logged
 * by exception type only (COMMANDO.md Sections 10 and 21).
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,
                            @Value("${app.jwt.expiration}") long accessTokenExpirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    /**
     * Builds an access token for the given user.
     *
     * @param email  becomes the {@code sub} claim
     * @param userId becomes the {@code userId} claim
     * @param role   becomes the {@code role} claim (ROLE_AUDITOR or ROLE_ADMIN)
     */
    public String generateAccessToken(String email, Long userId, String role) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + accessTokenExpirationMs);
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /** @return true when the token's signature, structure and expiry are all valid. */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // Log the reason type only — never the token itself.
            log.warn("Rejected JWT: {}", ex.getClass().getSimpleName());
            return false;
        }
    }

    /** @return the {@code sub} claim (the user's email). */
    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /** @return the {@code userId} claim. */
    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Number.class).longValue();
    }

    /** @return the {@code role} claim (ROLE_AUDITOR or ROLE_ADMIN). */
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
