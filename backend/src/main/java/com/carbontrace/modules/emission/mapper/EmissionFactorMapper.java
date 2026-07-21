package com.carbontrace.modules.emission.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.carbontrace.modules.emission.dto.EmissionFactorRequestDto;
import com.carbontrace.modules.emission.dto.EmissionFactorResponseDto;
import com.carbontrace.modules.emission.entity.EmissionFactor;

/**
 * MapStruct mapping for the emission module (COMMANDO.md Section 21: MapStruct
 * for ALL mapping — no manual field copying).
 */
@Mapper(componentModel = "spring")
public interface EmissionFactorMapper {

    /** Entity to response; {@code transportMode} converts from the enum to its name. */
    EmissionFactorResponseDto toResponseDto(EmissionFactor factor);

    /**
     * Request to a NEW factor row.
     *
     * <p>{@code circuityFactor} carries an explicit {@code defaultValue} because
     * the request may omit it and the column is NOT NULL. Without it MapStruct
     * would pass null to the builder, overwriting the entity's own
     * {@code @Builder.Default} of 1.20 and failing at insert — a builder default
     * only applies when the property is never set at all.
     *
     * <p>{@code isActive} is ignored so the entity's default (active) stands;
     * Section 8.5 gives that flag its own endpoint.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "circuityFactor", source = "circuityFactor", defaultValue = "1.20")
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    EmissionFactor toEntity(EmissionFactorRequestDto request);

    /**
     * Applies an edit onto an existing factor.
     *
     * <p>Same ignore set as {@link #toEntity}: an admin editing a factor must not
     * flip {@code isActive} through this route — Section 8.5 gives that its own
     * {@code toggle-active} endpoint — and {@code createdAt} must not be
     * rewritten by an update.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "circuityFactor", source = "circuityFactor", defaultValue = "1.20")
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateFactorFromDto(EmissionFactorRequestDto request, @MappingTarget EmissionFactor factor);
}
