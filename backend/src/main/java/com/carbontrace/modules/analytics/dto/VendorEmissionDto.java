package com.carbontrace.modules.analytics.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One vendor's total emissions — an element of the dashboard's
 * {@code emissionsByVendor} list (COMMANDO.md Section 8.9).
 *
 * <p>This class is also the target of a JPQL constructor expression in
 * {@code ShipmentRepository.sumEmissionsByVendor}: the {@code GROUP BY} rows map
 * straight into it, so the same shape serves both the query result and the
 * response element. The all-args constructor Lombok generates is what the
 * {@code SELECT new ...} expression calls, so its parameter order —
 * {@code (vendorId, vendorName, emissionsKgco2e)} — must not be reordered.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorEmissionDto {

    private Long vendorId;
    private String vendorName;
    private BigDecimal emissionsKgco2e;
}
