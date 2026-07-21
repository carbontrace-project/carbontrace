package com.carbontrace.modules.shipment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.carbontrace.modules.shipment.dto.ShipmentCreateRequest;
import com.carbontrace.modules.shipment.dto.ShipmentResponseDto;
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
}
