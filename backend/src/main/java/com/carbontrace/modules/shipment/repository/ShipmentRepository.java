package com.carbontrace.modules.shipment.repository;

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
}
