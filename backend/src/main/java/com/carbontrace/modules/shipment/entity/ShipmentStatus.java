package com.carbontrace.modules.shipment.entity;

/**
 * Lifecycle states of {@code shipments.status} (Section 7).
 *
 * <p>Section 9 fixes the legal transitions:
 * <pre>
 *   UPLOADED ──extraction ok──▶ NEEDS_REVIEW ──┐
 *      └──────extraction error──▶ FAILED ──────┼──review saved──▶ REVIEWED ──▶ CALCULATED
 * </pre>
 * {@link #CALCULATED} is terminal — the review endpoint rejects a shipment that
 * has reached it, which is what keeps a stored emissions figure consistent with
 * the fields it was computed from.
 *
 * <p>{@link #FAILED} is a recoverable state, not a dead end: extraction failed,
 * so the auditor fills every field in manually and the shipment rejoins the
 * normal path at REVIEWED (Section 8.4).
 *
 * <p>The transitions themselves are enforced by the service layer in STEP A018;
 * this enum only names the states.
 */
public enum ShipmentStatus {
    UPLOADED,
    NEEDS_REVIEW,
    REVIEWED,
    CALCULATED,
    FAILED
}
