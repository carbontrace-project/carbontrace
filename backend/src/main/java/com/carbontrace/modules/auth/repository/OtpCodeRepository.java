package com.carbontrace.modules.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.carbontrace.modules.auth.entity.OtpCode;
import com.carbontrace.modules.auth.entity.OtpPurpose;

/**
 * Data access for {@code otp_codes} (COMMANDO.md Section 9 OTP rules).
 */
@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    /**
     * The most recently issued unused code for an (email, purpose) pair — the
     * only code verification may consider. Expiry and attempt limits are applied
     * by the caller, not by this query, so an exhausted or expired code produces
     * a specific error message instead of a generic "invalid code".
     */
    Optional<OtpCode> findFirstByEmailAndPurposeAndIsUsedFalseOrderByCreatedAtDesc(
            String email, OtpPurpose purpose);

    /**
     * Marks every unused code for an (email, purpose) pair as used. Called before
     * issuing a new code so only the newest one can ever verify.
     *
     * @return the number of rows invalidated
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE OtpCode o SET o.isUsed = true "
            + "WHERE o.email = :email AND o.purpose = :purpose AND o.isUsed = false")
    int markAllAsUsed(@Param("email") String email, @Param("purpose") OtpPurpose purpose);
}
