package com.carbontrace.modules.emission.service;

import com.carbontrace.modules.shipment.dto.ShipmentResponseDto;

/**
 * Orchestrates {@code POST /api/shipments/{id}/calculate} (COMMANDO.md
 * Section 8.4).
 *
 * <p>Lives in the emission module rather than the shipment module because the
 * step it owns is the emission-factor lookup of Section 9; the shipment is its
 * input and its output. {@code ShipmentController} injects it directly, which
 * Section 8.4 dictates by putting the route under {@code /api/shipments}.
 *
 * <p>The arithmetic itself is NOT here. Section 24: "Emission calculation is
 * 100% deterministic in calculator.py". This service assembles the payload,
 * calls FastAPI, and persists what comes back — it never multiplies anything.
 */
public interface CalculationService {

    /**
     * Computes and stores a shipment's emissions.
     *
     * <p>Section 8.4, in order: require {@code REVIEWED} and the Section 9
     * calculation inputs; resolve the emission factor through the Section 9
     * fallback chain; POST the Section 8.11 payload to FastAPI
     * {@code /calculate}; persist {@code distance_km}, {@code distance_source}
     * and {@code total_emissions_kgco2e}; move the shipment to
     * {@code CALCULATED}.
     *
     * @param shipmentId the shipment to calculate
     * @return the shipment as stored after calculation
     * @throws com.carbontrace.exception.ResourceNotFoundException if no shipment has that id
     * @throws com.carbontrace.modules.shipment.exception.ShipmentException if the shipment is
     *         not {@code REVIEWED}, is missing a Section 9 calculation input, or no active
     *         emission factor matches — all 400 (Section 23)
     * @throws org.springframework.web.client.RestClientException if FastAPI cannot be reached
     *         or answers unusably — 502 "AI service unavailable — please try again"
     *         (Section 23), with the shipment left {@code REVIEWED}
     */
    ShipmentResponseDto calculateEmissions(Long shipmentId);
}
