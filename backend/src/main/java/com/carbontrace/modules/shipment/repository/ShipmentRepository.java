package com.carbontrace.modules.shipment.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
