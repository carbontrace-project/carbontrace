package com.carbontrace.modules.emission.serviceimpl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.carbontrace.config.AiConfig;
import com.carbontrace.exception.ResourceNotFoundException;
import com.carbontrace.modules.emission.dto.CalculationRequestDto;
import com.carbontrace.modules.emission.dto.CalculationResultDto;
import com.carbontrace.modules.emission.entity.EmissionFactor;
import com.carbontrace.modules.emission.repository.EmissionFactorRepository;
import com.carbontrace.modules.emission.service.CalculationService;
import com.carbontrace.modules.shipment.dto.ShipmentResponseDto;
import com.carbontrace.modules.shipment.entity.DistanceSource;
import com.carbontrace.modules.shipment.entity.Shipment;
import com.carbontrace.modules.shipment.entity.ShipmentStatus;
import com.carbontrace.modules.shipment.exception.ShipmentException;
import com.carbontrace.modules.shipment.mapper.ShipmentMapper;
import com.carbontrace.modules.shipment.repository.ShipmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The Section 8.4 calculation flow: validate, resolve the factor, call FastAPI,
 * persist.
 *
 * <p>{@code EmissionFactorRepository} and {@code ShipmentRepository} are injected
 * directly rather than their services, for the reason {@code ShipmentServiceImpl}
 * already documents: this class needs the ENTITIES — the factor's numbers to put
 * in the payload, the shipment to mutate — and both services return DTOs.
 *
 * <p><strong>The HTTP call sits inside the transaction.</strong> That holds a
 * database connection for the duration of a network round trip, which is a real
 * cost. It is done deliberately: Section 16 writes the sibling purchase flow the
 * same way ({@code @Transactional purchase(...)} with the FastAPI POST inside),
 * and splitting this one into read-then-call-then-write would leave the two
 * FastAPI call sites structurally different for no behavioural gain at this
 * project's scale. The read timeout is capped at 15s (Section 18) precisely so
 * the worst case is bounded.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalculationServiceImpl implements CalculationService {

    private static final String CALCULATE_PATH = "/calculate";

    private static final String SHIPMENT_NOT_FOUND = "Shipment not found with id: ";
    private static final String NOT_REVIEWED =
            "Shipment must be REVIEWED before its emissions can be calculated; current status: ";
    private static final String NO_TRANSPORT_MODE =
            "Calculation requires a transport mode — set it on the review screen";
    private static final String NO_WEIGHT =
            "Calculation requires a weight in tonnes greater than zero";
    private static final String NO_DISTANCE_OR_COORDINATES =
            "Calculation requires either a distance in km greater than zero, "
            + "or all four origin and destination coordinates";
    private static final String NO_FACTOR =
            "No active emission factor found for transport mode ";
    private static final String UNUSABLE_RESPONSE =
            "FastAPI /calculate returned an unusable body for shipment ";

    private final ShipmentRepository shipmentRepository;
    private final EmissionFactorRepository emissionFactorRepository;
    private final ShipmentMapper shipmentMapper;
    private final AiConfig aiConfig;

    /**
     * The 15s-read template of Section 18 Flow 5, not the 120s default one —
     * see {@code AppConfig}.
     */
    @Qualifier("calculationRestTemplate")
    private final RestTemplate calculationRestTemplate;

    /**
     * {@inheritDoc}
     *
     * <p>Ordering matters and is not arbitrary: every 400 check runs, and the
     * factor is resolved, BEFORE the network call. A shipment that cannot be
     * calculated should be told so in milliseconds, not after a 15-second wait
     * on a service whose answer would have been discarded.
     *
     * <p>Nothing is mutated until the response is in hand and validated, so the
     * 502 path of Section 23 leaves the shipment exactly {@code REVIEWED} —
     * without relying on the rollback to undo a half-written row. Section 23
     * reserves {@code FAILED} for extraction; a calculation failure is
     * retryable and must not consume the shipment's state.
     */
    @Override
    @Transactional
    public ShipmentResponseDto calculateEmissions(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> {
                    log.warn("Calculation failed — no shipment with id={}", shipmentId);
                    return new ResourceNotFoundException(SHIPMENT_NOT_FOUND + shipmentId);
                });

        requireCalculableState(shipment);
        EmissionFactor factor = resolveFactor(shipment);

        CalculationRequestDto request = buildRequest(shipment, factor);
        CalculationResultDto result = callAiService(request, shipmentId);

        applyResult(shipment, result);
        shipmentRepository.save(shipment);

        // The formula is the audit trail for the stored figure and has no column
        // of its own (Section 7 fixes the schema), so the log is where it lives.
        log.info("Shipment calculated: id={} factorId={} distanceKm={} source={} totalKgco2e={} formula=\"{}\"",
                shipmentId, factor.getId(), shipment.getDistanceKm(), shipment.getDistanceSource(),
                shipment.getTotalEmissionsKgco2e(), result.getFormula());

        return shipmentMapper.toResponseDto(shipment);
    }

    /**
     * The Section 9 preconditions, each with its own message so the auditor
     * learns which field to go back and fill in.
     */
    private void requireCalculableState(Shipment shipment) {
        if (shipment.getStatus() != ShipmentStatus.REVIEWED) {
            log.warn("Calculation rejected — shipment {} is in status {}",
                    shipment.getId(), shipment.getStatus());
            throw new ShipmentException(NOT_REVIEWED + shipment.getStatus());
        }
        if (shipment.getTransportMode() == null) {
            log.warn("Calculation rejected — shipment {} has no transport mode", shipment.getId());
            throw new ShipmentException(NO_TRANSPORT_MODE);
        }
        if (isNotPositive(shipment.getWeightTonnes())) {
            log.warn("Calculation rejected — shipment {} has no positive weight", shipment.getId());
            throw new ShipmentException(NO_WEIGHT);
        }
        if (isNotPositive(shipment.getDistanceKm()) && !hasAllCoordinates(shipment)) {
            log.warn("Calculation rejected — shipment {} has neither a distance nor full coordinates",
                    shipment.getId());
            throw new ShipmentException(NO_DISTANCE_OR_COORDINATES);
        }
    }

    /**
     * The Section 9 fallback chain, keyed on the shipment's ORIGIN COUNTRY.
     *
     * <p>The repository ranks the four candidates and returns them best first;
     * the head is the winner. A null {@code originCountry} or {@code fuelType}
     * needs no special case — SQL equality against null is unknown, so those
     * branches simply drop out and the chain degrades to the GLOBAL/ANY backstop
     * Section 9 requires to exist for exactly this reason.
     */
    private EmissionFactor resolveFactor(Shipment shipment) {
        String fuel = shipment.getFuelType() == null ? null : shipment.getFuelType().name();

        List<EmissionFactor> candidates = emissionFactorRepository.findWithFallback(
                shipment.getOriginCountry(), shipment.getTransportMode(), fuel);

        if (candidates.isEmpty()) {
            log.warn("Calculation rejected — no active emission factor for region={} mode={} fuel={}",
                    shipment.getOriginCountry(), shipment.getTransportMode(), fuel);
            throw new ShipmentException(NO_FACTOR + shipment.getTransportMode()
                    + " in region " + shipment.getOriginCountry());
        }

        EmissionFactor factor = candidates.get(0);
        log.info("Emission factor resolved for shipment {}: id={} {}/{}/{} factor={} circuity={}",
                shipment.getId(), factor.getId(), factor.getRegion(), factor.getTransportMode(),
                factor.getFuelType(), factor.getFactorKgco2ePerTonneKm(), factor.getCircuityFactor());
        return factor;
    }

    /**
     * Assembles the Section 8.11 payload — every value FastAPI needs, because
     * Section 24 forbids it from looking anything up for itself.
     *
     * <p>A non-positive {@code distanceKm} is sent as null rather than as itself:
     * null is the Section 15 instruction to compute the distance from the
     * coordinates, and a zero would otherwise be taken at face value and produce
     * zero emissions.
     */
    private CalculationRequestDto buildRequest(Shipment shipment, EmissionFactor factor) {
        return CalculationRequestDto.builder()
                .shipmentId(shipment.getId())
                .weightTonnes(shipment.getWeightTonnes())
                .distanceKm(isNotPositive(shipment.getDistanceKm()) ? null : shipment.getDistanceKm())
                .originLat(shipment.getOriginLat())
                .originLng(shipment.getOriginLng())
                .destinationLat(shipment.getDestinationLat())
                .destinationLng(shipment.getDestinationLng())
                .transportMode(shipment.getTransportMode().name())
                .emissionFactorKgco2ePerTonneKm(factor.getFactorKgco2ePerTonneKm())
                .circuityFactor(factor.getCircuityFactor())
                .build();
    }

    /**
     * The one network call. A connection failure surfaces as
     * {@link org.springframework.web.client.ResourceAccessException}, which the
     * global handler maps to 502 — this method does not catch it, because
     * swallowing it here would mean deciding the shipment's fate twice.
     *
     * <p>An answer that arrives but cannot be used is treated the same way, as an
     * upstream failure rather than a client error: the caller did nothing wrong,
     * and Section 23 gives 502 to "RestClientException from FastAPI calls".
     */
    private CalculationResultDto callAiService(CalculationRequestDto request, Long shipmentId) {
        String url = aiConfig.getFastapiBaseUrl() + CALCULATE_PATH;
        log.info("Calling FastAPI {} for shipment {}", CALCULATE_PATH, shipmentId);

        CalculationResultDto result = calculationRestTemplate.postForObject(
                url, request, CalculationResultDto.class);

        if (result == null || result.getTotalEmissionsKgco2e() == null || result.getDistanceKm() == null) {
            log.error("FastAPI {} returned an incomplete body for shipment {}", CALCULATE_PATH, shipmentId);
            throw new RestClientException(UNUSABLE_RESPONSE + shipmentId);
        }
        return result;
    }

    /**
     * Persists the three Section 8.4 result fields and closes the state machine.
     *
     * <p>{@code distance_source} is parsed strictly: an unrecognised value means
     * the two services disagree about the Section 7 enum, and storing null for it
     * would leave a distance whose provenance nobody can state. Section 25.2 puts
     * that class of disagreement on the contract, not on the data.
     */
    private void applyResult(Shipment shipment, CalculationResultDto result) {
        shipment.setDistanceKm(result.getDistanceKm());
        shipment.setDistanceSource(parseDistanceSource(result.getDistanceSource(), shipment.getId()));
        shipment.setTotalEmissionsKgco2e(result.getTotalEmissionsKgco2e());
        shipment.setStatus(ShipmentStatus.CALCULATED);
    }

    private DistanceSource parseDistanceSource(String value, Long shipmentId) {
        try {
            return DistanceSource.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException ex) {
            log.error("FastAPI {} returned distance_source '{}' for shipment {}, which is not a Section 7 value",
                    CALCULATE_PATH, value, shipmentId);
            throw new RestClientException(UNUSABLE_RESPONSE + shipmentId);
        }
    }

    /** Null and zero and negative all mean "not usable as an input" here. */
    private boolean isNotPositive(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) <= 0;
    }

    private boolean hasAllCoordinates(Shipment shipment) {
        return shipment.getOriginLat() != null && shipment.getOriginLng() != null
                && shipment.getDestinationLat() != null && shipment.getDestinationLng() != null;
    }
}
