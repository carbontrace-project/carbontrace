package com.carbontrace.modules.vendor.entity;

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

/**
 * Logistics vendor — maps to the {@code vendors} table (COMMANDO.md Section 7).
 *
 * <p>Vendors are never deleted (Section 9): {@link #isActive} is toggled instead,
 * because {@code shipments.vendor_id} is a required foreign key and deleting a
 * vendor would orphan its shipment history.
 *
 * <p>Follows the {@code User} entity's conventions: boxed {@link Boolean} so
 * Hibernate does not render {@code NOT NULL} where Section 7 says
 * {@code BOOLEAN DEFAULT}, and defaults declared twice — {@link ColumnDefault}
 * for the DDL, {@link Builder.Default} for objects built in Java.
 */
@Entity
@Table(name = "vendors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** Optional in Section 7 — the only nullable text column on this table. */
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "country", nullable = false, length = 60)
    private String country;

    /**
     * Always {@link VendorType#LOGISTICS} in the MVP (Section 9). Stored as a
     * VARCHAR via {@link EnumType#STRING} — Section 7 forbids native PostgreSQL
     * enum types.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'LOGISTICS'")
    @Column(name = "vendor_type", nullable = false, length = 20)
    private VendorType vendorType = VendorType.LOGISTICS;

    /** Soft-deactivation flag; inactive vendors cannot receive new shipments (STEP A017). */
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
