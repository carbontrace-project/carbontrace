package com.carbontrace.modules.shipment.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.carbontrace.common.ApiResponse;
import com.carbontrace.common.AppConstants;
import com.carbontrace.common.PagedResponse;
import com.carbontrace.modules.shipment.dto.ShipmentCreateRequest;
import com.carbontrace.modules.shipment.dto.ShipmentResponseDto;
import com.carbontrace.modules.shipment.dto.ShipmentReviewRequest;
import com.carbontrace.modules.shipment.dto.UploadUrlRequest;
import com.carbontrace.modules.shipment.dto.UploadUrlResponse;
import com.carbontrace.modules.shipment.entity.ShipmentStatus;
import com.carbontrace.modules.shipment.service.ShipmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The shipment endpoints of COMMANDO.md Section 8.4 that exist so far.
 *
 * <p>Still to come: review (STEP A018), calculate (A021) and the map endpoint
 * (A022). Nothing is stubbed for them.
 *
 * <p>Authorization needs no annotation — no route here matches a Section 10
 * permitAll or ROLE_ADMIN rule, so {@code anyRequest().authenticated()} covers
 * them all. Section 8.4 grants creation to ROLE_AUDITOR and ROLE_ADMIN and the
 * reads to any role, which for a two-role system is the same set. Section 8.4 is
 * explicit that "auditors and admins both see all shipments (single-company
 * tool)", so the list is not scoped to the caller.
 */
@Slf4j
@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    // Verbatim from the COMMANDO.md Section 8.4 example.
    private static final String UPLOAD_URL_MESSAGE = "Upload URL generated";

    // Section 8.4 quotes no message for these four; they follow its tone.
    private static final String DOCUMENT_URL_MESSAGE = "Document URL generated";
    private static final String CREATE_MESSAGE = "Shipment created successfully";
    private static final String LIST_MESSAGE = "Shipments retrieved";
    private static final String GET_MESSAGE = "Shipment retrieved";
    private static final String REVIEW_MESSAGE = "Shipment review saved";

    private final ShipmentService shipmentService;

    /** 200 — a signed, 10-minute PUT for a single PDF. Any authenticated role. */
    @PostMapping("/upload-url")
    public ApiResponse<UploadUrlResponse> createUploadUrl(@Valid @RequestBody UploadUrlRequest request) {
        // The file name is safe to log; the URL this produces is not (Section 21).
        log.info("Upload URL requested for fileName={}", request.getFileName());
        return ApiResponse.success(UPLOAD_URL_MESSAGE, shipmentService.createUploadUrl(request));
    }

    /**
     * 201 — registers an upload that already landed in S3.
     *
     * <p>The uploader is taken from the authenticated principal, never from the
     * body: a client that could name its own {@code uploaded_by} could attribute
     * a shipment to someone else.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ShipmentResponseDto> createShipment(@Valid @RequestBody ShipmentCreateRequest request,
                                                           @AuthenticationPrincipal String email) {
        log.info("Shipment creation requested by {} for vendorId={}", email, request.getVendorId());
        return ApiResponse.success(CREATE_MESSAGE, shipmentService.createShipment(request, email));
    }

    /**
     * 200 — paged list with optional {@code status} and {@code vendorId} filters.
     *
     * <p>{@code status} binds straight to the enum, so an unknown value is a 400
     * from the {@code MethodArgumentTypeMismatchException} handler rather than a
     * silently empty page.
     */
    @GetMapping
    public ApiResponse<PagedResponse<ShipmentResponseDto>> getShipments(
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        log.info("Shipment list requested: status={} vendorId={} page={} size={}", status, vendorId, page, size);
        return ApiResponse.success(LIST_MESSAGE, shipmentService.getShipments(status, vendorId, page, size));
    }

    /** 200 — a single shipment, or 404. Any authenticated role. */
    @GetMapping("/{id}")
    public ApiResponse<ShipmentResponseDto> getShipmentById(@PathVariable Long id) {
        log.info("Shipment requested: id={}", id);
        return ApiResponse.success(GET_MESSAGE, shipmentService.getShipmentById(id));
    }

    /**
     * 200 — saves the auditor's corrections and moves the shipment to REVIEWED.
     *
     * <p>Rejected with 400 once the shipment is CALCULATED (Section 9: that state
     * is terminal for review).
     */
    @PutMapping("/{id}/review")
    public ApiResponse<ShipmentResponseDto> reviewShipment(@PathVariable Long id,
                                                           @Valid @RequestBody ShipmentReviewRequest request) {
        // The body is the shipment's own extracted data — no secrets — but it is
        // large and uninteresting, so only the id is logged.
        log.info("Shipment review requested: id={}", id);
        return ApiResponse.success(REVIEW_MESSAGE, shipmentService.reviewShipment(id, request));
    }

    /** 200 — a fresh signed, 15-minute GET for the shipment's PDF, or 404. Any authenticated role. */
    @GetMapping("/{id}/document-url")
    public ApiResponse<Map<String, Object>> getDocumentUrl(@PathVariable Long id) {
        log.info("Document URL requested for shipment id={}", id);
        return ApiResponse.success(DOCUMENT_URL_MESSAGE, shipmentService.getDocumentUrl(id));
    }
}
