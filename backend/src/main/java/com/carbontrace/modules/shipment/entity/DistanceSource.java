package com.carbontrace.modules.shipment.entity;

/**
 * Provenance of {@code shipments.distance_km} (Section 7).
 *
 * <p>Set by the calculation step, never by the auditor: Section 15 has
 * {@code compute_distance} return {@link #DOCUMENT} when the request already
 * carried a distance, and {@link #COMPUTED} when it derived one from the
 * coordinates via haversine × circuity factor. Section 8.4 documents the auditor
 * side of that — leaving {@code distanceKm} null in a review means "compute it
 * for me".
 */
public enum DistanceSource {
    DOCUMENT,
    COMPUTED
}
