package com.carbontrace.modules.vendor.entity;

/**
 * Vendor categories of the Section 7 {@code vendors.vendor_type} column.
 *
 * <p><strong>Only {@link #LOGISTICS} is reachable in the MVP.</strong> COMMANDO.md
 * Section 2 states the column "exists for future use but the UI and flows only
 * handle LOGISTICS vendors", and Section 9 makes it a rule: "MVP vendors are
 * always type LOGISTICS". The other two constants are declared so the stored
 * strings are a closed set the day manufacturing and utility data arrive
 * (Section 2 lists both as future enhancements) — nothing in this module ever
 * assigns them, and no request DTO exposes the field.
 */
public enum VendorType {
    LOGISTICS,
    MANUFACTURING,
    UTILITY
}
