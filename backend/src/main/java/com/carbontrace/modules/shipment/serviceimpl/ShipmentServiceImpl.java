package com.carbontrace.modules.shipment.serviceimpl;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carbontrace.common.AppConstants;
import com.carbontrace.common.PagedResponse;
import com.carbontrace.exception.ResourceNotFoundException;
import com.carbontrace.modules.auth.entity.User;
import com.carbontrace.modules.auth.repository.UserRepository;
import com.carbontrace.modules.shipment.dto.ShipmentCreateRequest;
import com.carbontrace.modules.shipment.dto.ShipmentMapPointDto;
import com.carbontrace.modules.shipment.dto.ShipmentResponseDto;
import com.carbontrace.modules.shipment.dto.ShipmentReviewRequest;
import com.carbontrace.modules.shipment.dto.UploadUrlRequest;
import com.carbontrace.modules.shipment.dto.UploadUrlResponse;
import com.carbontrace.modules.shipment.entity.Shipment;
import com.carbontrace.modules.shipment.entity.ShipmentDocument;
import com.carbontrace.modules.shipment.entity.ShipmentStatus;
import com.carbontrace.modules.shipment.exception.ShipmentException;
import com.carbontrace.modules.shipment.mapper.ShipmentMapper;
import com.carbontrace.modules.shipment.repository.ShipmentDocumentRepository;
import com.carbontrace.modules.shipment.repository.ShipmentRepository;
import com.carbontrace.modules.shipment.service.S3Service;
import com.carbontrace.modules.shipment.service.ShipmentService;
import com.carbontrace.modules.vendor.entity.Vendor;
import com.carbontrace.modules.vendor.repository.VendorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Shipment registration and reads per COMMANDO.md Section 8.4.
 *
 * <p>{@code VendorRepository} and {@code UserRepository} are injected directly
 * rather than the corresponding services: this class needs the {@code Vendor}
 * and {@code User} ENTITIES to hang the associations on, and those services
 * return DTOs. Going through them would mean re-loading each entity by id
 * afterwards — the same query, twice.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private static final String SHIPMENT_NOT_FOUND = "Shipment not found with id: ";
    private static final String DOCUMENT_NOT_FOUND = "No document found for shipment with id: ";
    private static final String VENDOR_NOT_FOUND = "Vendor not found with id: ";
    private static final String UPLOADER_NOT_FOUND = "User not found with email: ";
    private static final String VENDOR_INACTIVE =
            "Shipments cannot be created for an inactive vendor: ";
    private static final String ALREADY_CALCULATED =
            "Shipment has already been calculated and can no longer be reviewed";
    private static final String NOT_REVIEWABLE = "Shipment cannot be reviewed in status: ";

    /**
     * The states {@code PUT /api/shipments/{id}/review} accepts (Section 9).
     *
     * <p>{@code UPLOADED} is absent on purpose: it means extraction has not
     * finished, so there is nothing yet for the auditor to correct. It becomes
     * reachable only when A-SWAP-1 wires the real extraction call.
     */
    private static final Set<ShipmentStatus> REVIEWABLE_STATUSES = EnumSet.of(
            ShipmentStatus.NEEDS_REVIEW,
            ShipmentStatus.REVIEWED,
            ShipmentStatus.FAILED);

    /** Newest first, by unique id so pages can never overlap — as in {@code VendorServiceImpl}. */
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "id");

    private final ShipmentRepository shipmentRepository;
    private final ShipmentDocumentRepository shipmentDocumentRepository;
    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final ShipmentMapper shipmentMapper;
    private final S3Service s3Service;

    @Override
    public UploadUrlResponse createUploadUrl(UploadUrlRequest request) {
        return s3Service.generatePresignedPut(
                request.getFileName(),
                request.getContentType(),
                request.getFileSizeBytes());
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Extraction is deliberately not called here.</strong> Section 8.4
     * describes this endpoint as synchronously invoking FastAPI {@code /extract},
     * but STEP A017 predates that service: the parallel model (Section 25) has
     * Track A build against {@code mocks/mock-ai} and swap in the real call at
     * A-SWAP-1 / STEP A034. Until then the shipment is created at
     * {@code NEEDS_REVIEW} with every extracted field null, which is exactly the
     * state Section 8.4 defines for a successful extraction that found nothing —
     * so the auditor can already open it and type the fields in by hand, and no
     * status transition has to be redefined when extraction arrives.
     *
     * <p>Both rows are written in ONE transaction. If the document insert trips
     * the UNIQUE constraint on {@code s3_key} (the same object registered twice),
     * the shipment insert rolls back with it — otherwise a 409 would leave an
     * orphan shipment pointing at no document.
     */
    @Override
    @Transactional
    public ShipmentResponseDto createShipment(ShipmentCreateRequest request, String uploaderEmail) {
        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> {
                    log.warn("Shipment creation failed — unknown vendor id={}", request.getVendorId());
                    return new ResourceNotFoundException(VENDOR_NOT_FOUND + request.getVendorId());
                });

        // Section 9: "Shipments cannot be created for inactive vendors."
        if (!Boolean.TRUE.equals(vendor.getIsActive())) {
            log.warn("Shipment creation rejected — vendor {} is inactive", vendor.getId());
            throw new ShipmentException(VENDOR_INACTIVE + vendor.getName());
        }

        User uploader = userRepository.findByEmail(uploaderEmail)
                .orElseThrow(() -> new ResourceNotFoundException(UPLOADER_NOT_FOUND + uploaderEmail));

        Shipment shipment = shipmentRepository.save(Shipment.builder()
                .vendor(vendor)
                .uploadedBy(uploader)
                .status(ShipmentStatus.NEEDS_REVIEW)
                .build());

        ShipmentDocument document = shipmentMapper.toDocument(request);
        document.setShipment(shipment);
        shipmentDocumentRepository.save(document);

        log.info("Shipment created: id={} vendorId={} status={} s3Key={}",
                shipment.getId(), vendor.getId(), shipment.getStatus(), document.getS3Key());
        return shipmentMapper.toResponseDto(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ShipmentResponseDto> getShipments(ShipmentStatus status, Long vendorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, NEWEST_FIRST);
        Page<Shipment> result = shipmentRepository.findByOptionalFilters(status, vendorId, pageable);

        List<ShipmentResponseDto> content = result.getContent().stream()
                .map(shipmentMapper::toResponseDto)
                .toList();

        return new PagedResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponseDto getShipmentById(Long id) {
        return shipmentMapper.toResponseDto(findShipment(id));
    }

    /**
     * {@inheritDoc}
     *
     * <p>The state check runs BEFORE the mapper touches anything, so a rejected
     * review leaves the shipment byte-identical — no partial write, nothing for
     * the exception to roll back.
     *
     * <p>{@code REVIEWED → REVIEWED} is deliberately legal: an auditor who spots
     * a second mistake before running the calculation can simply save again.
     * {@code FAILED → REVIEWED} is the manual-entry path Section 23 describes,
     * where extraction failed and the auditor types every field in by hand —
     * which is why {@code failure_reason} is cleared here. Leaving it set would
     * keep the "extraction failed" banner on a shipment a human has since fixed.
     */
    @Override
    @Transactional
    public ShipmentResponseDto reviewShipment(Long id, ShipmentReviewRequest request) {
        Shipment shipment = findShipment(id);
        ShipmentStatus current = shipment.getStatus();

        // Section 9: CALCULATED is terminal for review. Called out separately
        // from the general guard because it is the one rejection an auditor will
        // actually hit, and it needs to say why rather than list legal states.
        if (current == ShipmentStatus.CALCULATED) {
            log.warn("Review rejected — shipment {} is already calculated", id);
            throw new ShipmentException(ALREADY_CALCULATED);
        }
        if (!REVIEWABLE_STATUSES.contains(current)) {
            log.warn("Review rejected — shipment {} is in status {}", id, current);
            throw new ShipmentException(NOT_REVIEWABLE + current);
        }

        shipmentMapper.updateShipmentFromReview(request, shipment);
        shipment.setStatus(ShipmentStatus.REVIEWED);
        shipment.setFailureReason(null);
        shipmentRepository.save(shipment);

        log.info("Shipment reviewed: id={} {} -> {}", id, current, ShipmentStatus.REVIEWED);
        return shipmentMapper.toResponseDto(shipment);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The filtering is entirely the repository's; this method only maps. Both
     * halves of the Section 8.4 rule — CALCULATED, and all four coordinates —
     * are in the query, so a shipment that cannot be drawn is never loaded.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ShipmentMapPointDto> getMapPoints() {
        List<ShipmentMapPointDto> points = shipmentRepository.findMapPoints().stream()
                .map(shipmentMapper::toMapPointDto)
                .toList();

        log.info("Map points retrieved: {}", points.size());
        return points;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Resolved through {@code shipment_documents} in one query rather than
     * loading the shipment first. The endpoint's job is to produce a URL, and
     * "shipment does not exist" and "shipment has no document" both mean it
     * cannot — the same 404 either way.
     *
     * <p>Section 9 allows one document per shipment in the MVP, so the first row
     * is taken; the repository returns a list because nothing in the schema
     * enforces that rule.
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDocumentUrl(Long shipmentId) {
        List<ShipmentDocument> documents = shipmentDocumentRepository.findByShipmentId(shipmentId);
        if (documents.isEmpty()) {
            log.warn("Document URL requested for shipment {} which has no document", shipmentId);
            throw new ResourceNotFoundException(DOCUMENT_NOT_FOUND + shipmentId);
        }

        String url = s3Service.generatePresignedGet(documents.get(0).getS3Key());
        return Map.of(
                "documentUrl", url,
                "expiresInSeconds", AppConstants.PRESIGNED_GET_EXPIRY_MINUTES * 60);
    }

    private Shipment findShipment(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Shipment lookup failed for id={}", id);
                    return new ResourceNotFoundException(SHIPMENT_NOT_FOUND + id);
                });
    }
}
