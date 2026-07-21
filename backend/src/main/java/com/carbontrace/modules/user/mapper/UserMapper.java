package com.carbontrace.modules.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.carbontrace.modules.auth.entity.User;
import com.carbontrace.modules.user.dto.UserResponseDto;
import com.carbontrace.modules.user.dto.UserUpdateRequestDto;

/**
 * MapStruct mapping between {@link User} and the Section 8.2 DTOs
 * (COMMANDO.md Section 21: MapStruct for ALL mapping — no manual field copying).
 *
 * <p>The entity lives in {@code modules/auth/entity} because {@code auth} owns
 * the {@code users} table (Section 6). This module reads and updates it; it does
 * not own it.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Entity to response. {@code role} converts from the enum to its name
     * automatically; {@code password} has no counterpart in the DTO, so the hash
     * cannot be carried across.
     */
    UserResponseDto toResponseDto(User user);

    /**
     * Applies the three editable fields onto a managed {@link User}.
     *
     * <p>Every other column is ignored EXPLICITLY rather than by an
     * {@code unmappedTargetPolicy}, so the Section 8.2 immutability rule is
     * legible in the generated code and a later change to the DTO cannot quietly
     * widen what this method writes. {@code email}, {@code password} and
     * {@code role} are the three the spec names; the rest are ignored because
     * they are owned elsewhere — {@code isEmailVerified} by OTP verification,
     * {@code isActive} by the admin module (STEP A027), and the timestamps by
     * Hibernate.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "isEmailVerified", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUserFromDto(UserUpdateRequestDto request, @MappingTarget User user);
}
