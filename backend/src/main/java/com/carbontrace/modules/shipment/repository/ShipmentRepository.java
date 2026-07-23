package com.carbontrace.modules.shipment.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.carbontrace.modules.analytics.dto.VendorEmissionDto;
import com.carbontrace.modules.shipment.entity.Shipment;
import com.carbontrace.modules.shipment.entity.ShipmentStatus;

/**
 * Data access for {@code shipments}.
 *
 * <p>{@code GET /api/shipments} (Section 8.4) takes {@code status} and
 * {@code vendorId}, each independently optional — four combinations in all.
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    /**
     * One page of shipments, filtered by whichever of the two parameters is
     * non-null.
     *
     * <p>A single null-tolerant query rather than four derived methods
     * ({@code findAll}, {@code findByStatus}, {@code findByVendorId},
     * {@code findByStatusAndVendorId}) plus the branching in the service that
     * choosing between them requires. Section 24 counts that kind of
     * combinatorial spread as an unnecessary abstraction, and it grows as
     * {@code 2^n} the moment a third filter appears.
     *
     * <p>Both columns carry a Section 7 index ({@code idx_shipments_status},
     * {@code idx_shipments_vendor}), so each branch of this query is served by
     * one.
     *
     * @param status   filter by lifecycle state, or null for any
     * @param vendorId filter by vendor, or null for any
     * @param pageable page, size and sort
     */
    @Query("""
            SELECT s FROM Shipment s
            WHERE (:status IS NULL OR s.status = :status)
              AND (:vendorId IS NULL OR s.vendor.id = :vendorId)
            """)
    Page<Shipment> findByOptionalFilters(@Param("status") ShipmentStatus status,
                                         @Param("vendorId") Long vendorId,
                                         Pageable pageable);

    /**
     * Every shipment the emissions map can plot ({@code GET /api/shipments/map},
     * Section 8.4): status {@code CALCULATED} and all four coordinates present.
     *
     * <p>Both halves of that rule are in the WHERE clause rather than a filter
     * applied to {@code findAll()}. The map is the one unpaged read in the
     * application, so an in-memory filter would load every shipment ever
     * uploaded — documents, failures, half-reviewed rows — to draw a subset, and
     * the cost would grow with the table rather than with the map.
     *
     * <p>The status is a literal, not a parameter, because it is not a filter the
     * caller chooses: Section 8.4 defines this endpoint as returning CALCULATED
     * shipments only. A {@code :status} parameter would let a future caller ask
     * for lines the frontend has no emissions figure to weight.
     *
     * <p>A partial coordinate set is excluded rather than defaulted. Leaflet
     * given a null latitude draws nothing useful, and a route invented from the
     * two coordinates that happen to exist would be a line the shipment never
     * travelled — Section 24's "missing fields are null, never guessed" applies
     * to what is drawn as much as to what is extracted.
     *
     * <p>{@code idx_shipments_status} (Section 7) serves the status predicate.
     *
     * @return the plottable shipments, newest first
     */
    @Query("""
            SELECT s FROM Shipment s
            WHERE s.status = com.carbontrace.modules.shipment.entity.ShipmentStatus.CALCULATED
              AND s.originLat IS NOT NULL
              AND s.originLng IS NOT NULL
              AND s.destinationLat IS NOT NULL
              AND s.destinationLng IS NOT NULL
            ORDER BY s.id DESC
            """)
    List<Shipment> findMapPoints();

    // ----------------------------------------------------- STEP A024 analytics
    // The dashboard aggregates of COMMANDO.md Section 8.9, all computed here so
    // "nothing is stored separately". Every emission aggregate is scoped to
    // CALCULATED shipments — the only ones with a figure to sum — using the same
    // fully-qualified enum literal findMapPoints already uses. SUM returns null
    // for an empty set (not zero); the service coalesces, so the empty-database
    // dashboard is zeros rather than a NullPointerException.

    /** {@code calculatedShipments} (Section 8.9); {@code totalShipments} is {@link #count()}. */
    long countByStatus(ShipmentStatus status);

    /** Lifetime total emissions over CALCULATED shipments; null if there are none. */
    @Query("""
            SELECT SUM(s.totalEmissionsKgco2e) FROM Shipment s
            WHERE s.status = com.carbontrace.modules.shipment.entity.ShipmentStatus.CALCULATED
            """)
    BigDecimal sumCalculatedEmissions();

    /**
     * Lifetime offset tonnes across ALL shipments; null if the table is empty.
     *
     * <p>Not scoped to CALCULATED: {@code offset_tonnes} defaults to zero and is
     * only ever raised by a purchase (STEP A026), so summing every row is both
     * correct and future-proof. Today it is zero everywhere.
     */
    @Query("SELECT SUM(s.offsetTonnes) FROM Shipment s")
    BigDecimal sumOffsetTonnes();

    /**
     * Emissions grouped by transport mode ({@code emissionsByMode}). Each row is
     * {@code [TransportMode, BigDecimal]}; the service turns it into the
     * Section 8.9 map keyed by the enum name.
     */
    @Query("""
            SELECT s.transportMode, SUM(s.totalEmissionsKgco2e) FROM Shipment s
            WHERE s.status = com.carbontrace.modules.shipment.entity.ShipmentStatus.CALCULATED
            GROUP BY s.transportMode
            """)
    List<Object[]> sumEmissionsByMode();

    /**
     * Emissions grouped by vendor ({@code emissionsByVendor}), highest first.
     *
     * <p>A JPQL constructor expression maps each {@code GROUP BY} row straight
     * into {@link VendorEmissionDto}, so no intermediate {@code Object[]}
     * handling is needed here.
     */
    @Query("""
            SELECT new com.carbontrace.modules.analytics.dto.VendorEmissionDto(
                       s.vendor.id, s.vendor.name, SUM(s.totalEmissionsKgco2e))
            FROM Shipment s
            WHERE s.status = com.carbontrace.modules.shipment.entity.ShipmentStatus.CALCULATED
            GROUP BY s.vendor.id, s.vendor.name
            ORDER BY SUM(s.totalEmissionsKgco2e) DESC
            """)
    List<VendorEmissionDto> sumEmissionsByVendor();

    /**
     * Emissions grouped by the year and month of {@code shipment_date}, from
     * {@code windowStart} onward ({@code monthlyEmissions}).
     *
     * <p>Each row is {@code [Integer year, Integer month, BigDecimal]}; the
     * service places it into the fixed six-month, zero-filled window so the
     * result is always exactly six {@code yyyy-MM} keys (Section 8.9).
     * {@code shipment_date} is the trend's axis because it is when the freight
     * moved; a CALCULATED shipment with no date is legitimately absent from the
     * timeline while still counting in the lifetime totals above.
     */
    @Query("""
            SELECT YEAR(s.shipmentDate), MONTH(s.shipmentDate), SUM(s.totalEmissionsKgco2e)
            FROM Shipment s
            WHERE s.status = com.carbontrace.modules.shipment.entity.ShipmentStatus.CALCULATED
              AND s.shipmentDate IS NOT NULL
              AND s.shipmentDate >= :windowStart
            GROUP BY YEAR(s.shipmentDate), MONTH(s.shipmentDate)
            """)
    List<Object[]> sumEmissionsByMonth(@Param("windowStart") LocalDate windowStart);

    /** Current-year emissions for the Section 9 goal-progress numerator; null if none. */
    @Query("""
            SELECT SUM(s.totalEmissionsKgco2e) FROM Shipment s
            WHERE s.status = com.carbontrace.modules.shipment.entity.ShipmentStatus.CALCULATED
              AND YEAR(s.shipmentDate) = :year
            """)
    BigDecimal sumEmissionsForYear(@Param("year") int year);

    /** Current-year offset tonnes for the Section 9 goal-progress numerator; null if none. */
    @Query("""
            SELECT SUM(s.offsetTonnes) FROM Shipment s
            WHERE s.status = com.carbontrace.modules.shipment.entity.ShipmentStatus.CALCULATED
              AND YEAR(s.shipmentDate) = :year
            """)
    BigDecimal sumOffsetTonnesForYear(@Param("year") int year);
}
