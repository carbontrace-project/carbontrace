package com.carbontrace.modules.vendor.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.carbontrace.modules.vendor.dto.VendorRequestDto;
import com.carbontrace.modules.vendor.dto.VendorResponseDto;
import com.carbontrace.modules.vendor.entity.Vendor;

/**
 * MapStruct mapping for the vendor module (COMMANDO.md Section 21: MapStruct for
 * ALL mapping — no manual field copying).
 */
@Mapper(componentModel = "spring")
public interface VendorMapper {

    /** Entity to response; {@code vendorType} converts from the enum to its name. */
    VendorResponseDto toResponseDto(Vendor vendor);

    /**
     * Request to a NEW entity for {@code POST /api/vendors}.
     *
     * <p>{@code vendorType} and {@code isActive} are ignored so the entity's own
     * {@code @Builder.Default}/field initialisers stand — LOGISTICS and active,
     * per Section 9. The timestamps belong to Hibernate.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vendorType", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Vendor toEntity(VendorRequestDto request);

    /**
     * Applies an edit onto an existing vendor for {@code PUT /api/vendors/{id}}.
     *
     * <p>Same ignore set as {@link #toEntity}, for a sharper reason: an admin
     * editing a vendor must not be able to flip {@code isActive} through this
     * route — Section 8.3 gives that its own {@code toggle-active} endpoint —
     * and must not be able to change {@code vendorType} away from LOGISTICS.
     * {@code createdAt} is ignored so an update cannot rewrite history.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vendorType", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateVendorFromDto(VendorRequestDto request, @MappingTarget Vendor vendor);
}
