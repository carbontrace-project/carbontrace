package com.carbontrace.modules.shipment.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The uploaded PDF behind a shipment — maps to {@code shipment_documents}
 * (COMMANDO.md Section 7).
 *
 * <p>This row holds only the S3 POINTER and the file's metadata. The bytes never
 * pass through Spring Boot: the browser PUTs them straight to a private bucket
 * with a presigned URL, and every later read is another presigned URL
 * (Sections 17 and 18).
 *
 * <p>Section 9 allows exactly one document per shipment in the MVP, but the
 * relationship is modelled {@code @ManyToOne} to match Section 7's
 * "Shipment (1) ──── (N) ShipmentDocument" and Section 24's "prefer ManyToOne".
 * The one-per-shipment rule is a service-layer check (STEP A017), not a schema
 * constraint — a unique index on {@code shipment_id} would have to be dropped
 * the day multi-document uploads arrive.
 *
 * <p>There is no {@code updated_at}: Section 7 gives this table
 * {@code created_at} only. A document is written once and never edited — a
 * re-upload is a new object under a new S3 key.
 */
@Entity
@Table(name = "shipment_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    /**
     * Object key inside the private bucket, following the Section 17 convention
     * {@code invoices/{yyyy}/{MM}/{uuid}-{sanitizedFileName}} — hence the
     * generous {@code VARCHAR(300)}.
     *
     * <p>UNIQUE per Section 7: the key already contains a UUID, so a collision
     * means the same object is being registered twice. The constraint turns that
     * into a 409 through the {@code DataIntegrityViolationException} mapping
     * (Section 23) instead of two rows pointing at one file.
     *
     * <p>This is a key, not a presigned URL — no signature, no credentials, safe
     * to store and to log.
     */
    @Column(name = "s3_key", nullable = false, unique = true, length = 300)
    private String s3Key;

    /** The auditor's original file name, kept for display; the S3 key holds the sanitised form. */
    @Column(name = "file_name", nullable = false, length = 200)
    private String fileName;

    /** Always {@code application/pdf} in the MVP — Section 9 accepts nothing else. */
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    /** Nullable in Section 7: the browser reports it, so it is informational, not trusted. */
    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
