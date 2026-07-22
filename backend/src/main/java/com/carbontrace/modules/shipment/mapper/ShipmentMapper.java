package com.carbontrace.modules.shipment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.carbontrace.modules.shipment.dto.ShipmentCreateRequest;
import com.carbontrace.modules.shipment.dto.ShipmentMapPointDto;
import com.carbontrace.modules.shipment.dto.ShipmentResponseDto;
import com.carbontrace.modules.shipment.dto.ShipmentReviewRequest;
import com.carbontrace.modules.shipment.entity.Shipment;
import com.carbontrace.modules.shipment.entity.ShipmentDocument;

/**
 * MapStruct mapping for the shipment module (COMMANDO.md Section 21: MapStruct
 * for ALL mapping — no manual field copying).
 */
@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    /**
     * Entity to response. The four association fields are flattened by path; the
     * five enum columns convert to their names automatically.
     */
    @Mapping(target = "vendorId", source = "vendor.id")
    @Mapping(target = "vendorName", source = "vendor.name")
    @Mapping(target = "uploadedById", source = "uploadedBy.id")
    @Mapping(target = "uploadedByEmail", source = "uploadedBy.email")
    ShipmentResponseDto toResponseDto(Shipment shipment);

    /**
     * Entity to one map marker pair ({@code GET /api/shipments/map},
     * Section 8.4).
     *
     * <p>Every target field has a same-named source, so there is nothing to
     * declare: the two enums convert to their names exactly as they do in
     * {@link #toResponseDto}. The narrowing is the point of the DTO, not of the
     * mapping.
     */
    ShipmentMapPointDto toMapPointDto(Shipment shipment);

    /**
     * Request to the {@link ShipmentDocument} row that records what was uploaded.
     *
     * <p>{@code contentType} is a CONSTANT, not a mapped field: Section 9 accepts
     * only {@code application/pdf}, and STEP A016 already signed the upload with
     * that content type, so S3 refused anything else at PUT time. Writing the
     * constant here means the stored value cannot disagree with the value the
     * object was actually stored under.
     *
     * <p>{@code shipment} is ignored because the parent is assigned by the
     * service after it has been persisted and has an id.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shipment", ignore = true)
    @Mapping(target = "contentType", constant = "application/pdf")
    @Mapping(target = "createdAt", ignore = true)
    ShipmentDocument toDocument(ShipmentCreateRequest request);

    /**
     * Applies the auditor's corrections onto an existing shipment
     * ({@code PUT /api/shipments/{id}/review}, Section 8.4).
     *
     * <p>The two enum columns are populated from Strings; MapStruct generates the
     * {@code valueOf} conversion, and the DTO's {@code @Pattern} constraints have
     * already guaranteed the value is in range by the time this runs.
     *
     * <p>Every other target is ignored EXPLICITLY, and each for its own reason:
     * <ul>
     *   <li>{@code vendor}, {@code uploadedBy}, {@code id}, {@code createdAt} —
     *       established at creation; a review is a correction, not a re-assignment.</li>
     *   <li>{@code distanceSource} — Section 8.4 defers it to calculation, where
     *       Section 15 decides DOCUMENT vs COMPUTED. Writing it here would
     *       pre-empt that decision.</li>
     *   <li>{@code extractionConfidence} — describes what the AI produced, not
     *       what the auditor typed. Overwriting it would erase the record of how
     *       trustworthy the original extraction was.</li>
     *   <li>{@code totalEmissionsKgco2e}, {@code offsetTonnes} — owned by
     *       calculation (A021) and purchasing (A026).</li>
     *   <li>{@code status}, {@code failureReason} — the service owns the state
     *       machine; see {@code ShipmentServiceImpl.reviewShipment}.</li>
     *   <li>{@code updatedAt} — Hibernate's.</li>
     * </ul>
     *
     * <p>Null source fields ARE copied. Review is a full replacement of the
     * editable set, so clearing a field the AI wrongly populated is a legitimate
     * correction — the auditor's blank must win over the model's guess.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    @Mapping(target = "distanceSource", ignore = true)
    @Mapping(target = "extractionConfidence", ignore = true)
    @Mapping(target = "totalEmissionsKgco2e", ignore = true)
    @Mapping(target = "offsetTonnes", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateShipmentFromReview(ShipmentReviewRequest request, @MappingTarget Shipment shipment);
}
