package com.carbontrace.modules.shipment.entity;

/**
 * Fuel types of the Section 7 {@code shipments.fuel_type} column.
 *
 * <p>{@link #UNKNOWN} is a real, storable value, not a placeholder: Section 14
 * tells the extractor to normalise an unrecognised fuel to "the allowed enum (or
 * null / UNKNOWN)". It is distinct from a null column — null means the document
 * did not mention a fuel, UNKNOWN means it did and the value was not one of
 * these.
 *
 * <p>The emission-factor lookup chain (Section 9) falls back to the {@code 'ANY'}
 * wildcard row when no factor matches a specific fuel, so neither null nor
 * UNKNOWN can dead-end a calculation.
 */
public enum FuelType {
    DIESEL,
    PETROL,
    LNG,
    JET_FUEL,
    HEAVY_FUEL_OIL,
    ELECTRIC,
    UNKNOWN
}
