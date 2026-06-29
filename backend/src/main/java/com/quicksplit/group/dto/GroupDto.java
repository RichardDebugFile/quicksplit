package com.quicksplit.group.dto;

import com.quicksplit.user.dto.UserDto;
import java.time.Instant;
import java.util.List;

/**
 * Detalle de un grupo con sus miembros.
 */
public record GroupDto(
        Long id,
        String name,
        String description,
        Long ownerId,
        Instant createdAt,
        List<UserDto> members) {
}
