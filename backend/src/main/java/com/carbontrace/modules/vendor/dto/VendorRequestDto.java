package com.carbontrace.modules.vendor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body of {@code POST /api/vendors} and {@code PUT /api/vendors/{id}}
 * (COMMANDO.md Section 8.3).
 *
 * <p>The three fields are exactly those in the Section 8.3 example. Two Section 7
 * columns are deliberately absent:
 * <ul>
 *   <li>{@code vendorType} — Section 9: "MVP vendors are always type LOGISTICS".
 *       The server assigns it; a client cannot choose it.</li>
 *   <li>{@code isActive} — owned by {@code PUT /api/vendors/{id}/toggle-active},
 *       which is an ADMIN route. Accepting the flag here would let that
 *       dedicated endpoint be bypassed.</li>
 * </ul>
 * As in {@code UserUpdateRequestDto}, absence from the DTO is the enforcement:
 * the values cannot bind onto anything.
 *
 * <p>The {@code @Size(max)} caps match the Section 7 column widths, so an
 * over-long value is a 400 with a clear message rather than a database error.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorRequestDto {

    @NotBlank(message = "Vendor name is required")
    @Size(max = 100, message = "Vendor name must not exceed 100 characters")
    private String name;

    /**
     * Optional — Section 7 leaves {@code contact_email} nullable and Section 9
     * does not require it. There is no {@code @NotBlank}: omitting the field
     * entirely is valid, but a value that IS supplied must be a real address.
     */
    @Email(message = "Contact email must be a valid email address")
    @Size(max = 100, message = "Contact email must not exceed 100 characters")
    private String contactEmail;

    @NotBlank(message = "Country is required")
    @Size(max = 60, message = "Country must not exceed 60 characters")
    private String country;
}
