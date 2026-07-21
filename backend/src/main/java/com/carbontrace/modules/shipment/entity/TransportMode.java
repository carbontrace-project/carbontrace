package com.carbontrace.modules.shipment.entity;

/**
 * Freight transport modes of the Section 7 {@code shipments.transport_mode}
 * column.
 *
 * <p>These four values are also the {@code transport_mode} axis of
 * {@code emission_factors} (Section 7) and the mode the FastAPI extractor must
 * normalise to (Section 14), so the set is closed — a document naming anything
 * else yields null, never a new constant.
 */
public enum TransportMode {
    ROAD,
    RAIL,
    SEA,
    AIR
}
