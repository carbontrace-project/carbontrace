package com.carbontrace.modules.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.carbontrace.modules.auth.entity.RefreshToken;
import com.carbontrace.modules.auth.entity.User;

/**
 * Data access for {@code refresh_tokens}.
 *
 * <p>Tokens are revoked rather than deleted so the rotation history stays
 * inspectable (COMMANDO.md Section 8.1).
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /**
     * Revokes every still-valid token belonging to a user — used when all of a
     * user's sessions must end at once (deactivation, password reset).
     *
     * @return the number of rows revoked
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RefreshToken r SET r.isRevoked = true WHERE r.user = :user AND r.isRevoked = false")
    int revokeAllByUser(@Param("user") User user);
}
