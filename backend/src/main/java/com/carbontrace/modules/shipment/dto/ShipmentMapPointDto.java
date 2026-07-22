package com.carbontrace.modules.shipment.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One plottable route for {@code GET /api/shipments/map} (COMMANDO.md
 * Section 8.4).
 *
 * <p>Deliberately narrower than {@link ShipmentResponseDto}: Section 12 draws
 * "markers at origin and destination and a polyline between them; line
 * color/weight scaled by emissions; popup with route, mode, kgCO2e". These nine
 * fields are exactly that and nothing more. The map is the one screen that loads
 * every calculated shipment at once and is not paged, so every field it does not
 * draw is weight on the wire multiplied by the whole table.
 *
 * <p>Enums are Strings and emissions are {@link BigDecimal}, matching how
 * {@code ShipmentResponseDto} already serialises them — the same shipment must
 * not look different depending on which endpoint returned it.
 *
 * <p>{@code status} is here even though every row this endpoint returns is
 * {@code CALCULATED}. Section 8.4 lists it, so it is present: a client that
 * checks the field it was promised should not have to infer it from the URL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentMapPointDto {

    private Long id;

    private Double originLat;
    private Double originLng;
    private Double destinationLat;
    private Double destinationLng;

    private String originCity;
    private String destinationCity;

    private String transportMode;

    /** Drives the polyline's colour and weight (Section 12). */
    private BigDecimal totalEmissionsKgco2e;

    private String status;
}
