package com.carbontrace.modules.emission.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.carbontrace.modules.emission.entity.EmissionFactor;
import com.carbontrace.modules.shipment.entity.TransportMode;

/**
 * Data access for {@code emission_factors}.
 */
@Repository
public interface EmissionFactorRepository extends JpaRepository<EmissionFactor, Long> {

    /** The Section 7 wildcard that makes a fuel-agnostic factor row possible. */
    String ANY_FUEL = "ANY";

    /** The Section 9 last-resort region. */
    String GLOBAL_REGION = "GLOBAL";

    /**
     * The Section 9 fallback chain, as ONE query.
     *
     * <p>Candidates are the four combinations Section 9 permits, ranked by its
     * priority order, most specific first:
     * <ol>
     *   <li>{@code (region, mode, fuel)} — an exact match</li>
     *   <li>{@code (region, mode, 'ANY')} — right country, any fuel</li>
     *   <li>{@code ('GLOBAL', mode, fuel)} — right fuel, anywhere</li>
     *   <li>{@code ('GLOBAL', mode, 'ANY')} — the seeded backstop that
     *       Section 9 requires to exist for all four modes so calculation can
     *       never dead-end</li>
     * </ol>
     *
     * <p>One query rather than four sequential lookups: the ordering IS the
     * business rule, so expressing it in the query keeps the rule in one place
     * and costs one round trip instead of up to four.
     *
     * <p>{@code isActive = true} is part of the WHERE clause, not a filter
     * applied afterwards — Section 9 says "Only active factors participate in
     * lookup", so a deactivated exact match must not shadow an active fallback.
     *
     * <p>A null {@code fuel} behaves correctly without a special case: SQL
     * equality against null is unknown, so the two fuel-specific branches drop
     * out and the chain degrades to {@code (region, ANY)} then
     * {@code (GLOBAL, ANY)}. That is exactly right for a shipment whose document
     * never stated a fuel.
     *
     * <p>Returns a ranked {@link List} rather than a single row so the caller can
     * see what it matched; the service takes the head. JPQL has no LIMIT, and a
     * {@code Pageable} parameter here would obscure the intent for one row.
     *
     * @param region the shipment's origin country
     * @param mode   the shipment's transport mode
     * @param fuel   the shipment's fuel type name, or null if unknown
     * @return matching active factors, best first; empty if even the GLOBAL/ANY
     *         backstop is missing or inactive
     */
    @Query("""
            SELECT f FROM EmissionFactor f
            WHERE f.isActive = true
              AND f.transportMode = :mode
              AND ( (f.region = :region AND f.fuelType = :fuel)
                 OR (f.region = :region AND f.fuelType = 'ANY')
                 OR (f.region = 'GLOBAL' AND f.fuelType = :fuel)
                 OR (f.region = 'GLOBAL' AND f.fuelType = 'ANY') )
            ORDER BY CASE
                       WHEN f.region = :region AND f.fuelType = :fuel  THEN 1
                       WHEN f.region = :region AND f.fuelType = 'ANY'  THEN 2
                       WHEN f.region = 'GLOBAL' AND f.fuelType = :fuel THEN 3
                       ELSE 4
                     END
            """)
    List<EmissionFactor> findWithFallback(@Param("region") String region,
                                          @Param("mode") TransportMode mode,
                                          @Param("fuel") String fuel);

    /**
     * One page of factors, filtered by whichever of the two parameters is
     * non-null ({@code GET /api/emission-factors}, Section 8.5).
     *
     * <p>Inactive rows are included: an admin must be able to find a deactivated
     * factor in order to switch it back on.
     */
    @Query("""
            SELECT f FROM EmissionFactor f
            WHERE (:region IS NULL OR f.region = :region)
              AND (:mode IS NULL OR f.transportMode = :mode)
            """)
    Page<EmissionFactor> findByOptionalFilters(@Param("region") String region,
                                               @Param("mode") TransportMode mode,
                                               Pageable pageable);
}
