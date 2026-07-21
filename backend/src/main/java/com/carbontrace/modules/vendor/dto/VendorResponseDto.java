package com.carbontrace.modules.vendor.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for every {@code /api/vendors} endpoint (COMMANDO.md
 * Section 8.3), and the element type inside {@code PagedResponse} for the list.
 *
 * <p>Section 8.3 names {@code id}, {@code vendorType} and {@code isActive}
 * explicitly; the rest are the remaining Section 7 columns.
 *
 * <p>{@code vendorType} is a String rather than the enum, matching how
 * {@code UserResponseDto} serialises {@code role} — the client sees
 * {@code "LOGISTICS"}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorResponseDto {

    private Long id;
    private String name;
    private String contactEmail;
    private String country;
    private String vendorType;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
