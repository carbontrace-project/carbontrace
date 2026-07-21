package com.carbontrace.modules.shipment.entity;

/**
 * Extraction confidence stored in {@code shipments.extraction_confidence}
 * (Section 7) — the OVERALL confidence of an extraction, not a per-field one.
 *
 * <p>Section 14 defines how the overall level is derived: LOW if any core field
 * (weight, mode, origin, destination) is LOW or null; otherwise MEDIUM if any
 * core field is MEDIUM; otherwise HIGH. Per-field confidences are returned by
 * FastAPI for the review UI to highlight, but Section 7 gives the table a single
 * column, so only the rolled-up value is persisted.
 */
public enum ConfidenceLevel {
    HIGH,
    MEDIUM,
    LOW
}
