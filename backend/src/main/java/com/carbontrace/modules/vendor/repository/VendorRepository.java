package com.carbontrace.modules.vendor.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carbontrace.modules.vendor.entity.Vendor;

/**
 * Data access for {@code vendors}.
 *
 * <p>Paged {@code findAll(Pageable)} comes from {@link JpaRepository}; the only
 * addition is the optional name filter of {@code GET /api/vendors?search=}
 * (COMMANDO.md Section 8.3).
 */
@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    /**
     * Case-insensitive "contains" match on the vendor name.
     *
     * <p>Section 8.3 describes {@code search} only as an "optional name filter",
     * so it is deliberately forgiving: a user typing "ocean" finds
     * "OceanBridge Freight". Derived-query naming keeps this a one-liner — no
     * {@code @Query} and no Specification/Criteria machinery, which Section 24
     * would count as an unnecessary abstraction for a single filter.
     *
     * <p>Inactive vendors are included. Soft-deactivated rows must stay visible
     * so an admin can find one and toggle it back on (Section 9); filtering them
     * out here would make deactivation indistinguishable from deletion.
     */
    Page<Vendor> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
