package com.carbontrace.modules.shipment.service;

import java.util.List;
import java.util.Map;

import com.carbontrace.common.PagedResponse;
import com.carbontrace.modules.shipment.dto.ShipmentCreateRequest;
import com.carbontrace.modules.shipment.dto.ShipmentMapPointDto;
import com.carbontrace.modules.shipment.dto.ShipmentResponseDto;
import com.carbontrace.modules.shipment.dto.ShipmentReviewRequest;
import com.carbontrace.modules.shipment.dto.UploadUrlRequest;
import com.carbontrace.modules.shipment.dto.UploadUrlResponse;
import com.carbontrace.modules.shipment.entity.ShipmentStatus;

/**
 * The shipment operations reachable from {@code ShipmentController}
 * (COMMANDO.md Section 8.4).
 *
 * <p>Calculation is deliberately absent: Section 6 puts {@code CalculationService}
 * in the emission module, and {@code ShipmentController} injects it directly.
 */
public interface ShipmentService {

    /**
     * Issues a presigned {@code PUT} so the browser can upload a freight bill
     * straight to S3 (Section 8.4).
     *
     * @param request file name, content type, and optionally the declared size
     * @return the signed URL, the generated S3 key, and the expiry in seconds
     * @throws com.carbontrace.exception.BadRequestException if the file is not a PDF
     *         or the declared size exceeds 10 MB (Section 9)
     */
    UploadUrlResponse createUploadUrl(UploadUrlRequest request);

    /**
     * Registers an upload that has already landed in S3, creating the
     * {@code shipments} row and its one {@code shipment_documents} row.
     *
     * <p>The new shipment is {@code NEEDS_REVIEW} with every extracted field
     * null. Extraction is NOT called here — see the implementation note.
     *
     * @param request       vendor, S3 key, file name and declared size
     * @param uploaderEmail the authenticated caller, who becomes {@code uploaded_by}
     * @return the created shipment
     * @throws com.carbontrace.exception.ResourceNotFoundException if the vendor or the
     *         uploading user does not exist
     * @throws com.carbontrace.modules.shipment.exception.ShipmentException if the vendor
     *         is inactive (Section 9)
     */
    ShipmentResponseDto createShipment(ShipmentCreateRequest request, String uploaderEmail);

    /**
     * Lists shipments, newest first, filtered by whichever parameters are given.
     *
     * <p>Section 8.4: "Auditors and admins both see all shipments (single-company
     * tool)" — there is no per-user scoping.
     *
     * @param status   filter by lifecycle state, or null for any
     * @param vendorId filter by vendor, or null for any
     * @param page     zero-based page index
     * @param size     page size
     * @return one page of shipments in the {@link PagedResponse} envelope
     */
    PagedResponse<ShipmentResponseDto> getShipments(ShipmentStatus status, Long vendorId, int page, int size);

    /**
     * @param id shipment primary key
     * @return the shipment
     * @throws com.carbontrace.exception.ResourceNotFoundException if no shipment has that id
     */
    ShipmentResponseDto getShipmentById(Long id);

    /**
     * Saves the auditor's corrections to the extracted fields and moves the
     * shipment to {@code REVIEWED} (Section 8.4).
     *
     * <p>Permitted from {@code NEEDS_REVIEW}, {@code REVIEWED} and {@code FAILED}
     * only. Section 9 makes {@code CALCULATED} terminal for review: once
     * emissions have been computed from a set of fields, letting those fields
     * change would leave a stored figure that no longer follows from its inputs.
     *
     * @param id      shipment primary key
     * @param request the corrected fields; every one is optional
     * @return the shipment as stored after the review
     * @throws com.carbontrace.exception.ResourceNotFoundException if no shipment has that id
     * @throws com.carbontrace.modules.shipment.exception.ShipmentException if the shipment
     *         is CALCULATED, or in any other state review does not accept
     */
    ShipmentResponseDto reviewShipment(Long id, ShipmentReviewRequest request);

    /**
     * Every shipment the emissions map can draw (Section 8.4): status
     * {@code CALCULATED} with all four coordinates present.
     *
     * <p>Unpaged, because the map renders the whole set at once — Section 12
     * describes one view of all routes, not a page of them.
     *
     * @return the plottable shipments, newest first; empty when nothing has been
     *         calculated yet, which is a normal state, not an error
     */
    List<ShipmentMapPointDto> getMapPoints();

    /**
     * Issues a fresh presigned {@code GET} for a shipment's stored document
     * (Section 8.4).
     *
     * @param shipmentId the shipment whose document is wanted
     * @return {@code documentUrl} and {@code expiresInSeconds}
     * @throws com.carbontrace.exception.ResourceNotFoundException if the shipment has
     *         no document registered
     */
    Map<String, Object> getDocumentUrl(Long shipmentId);
}
