package com.quicksplit.group.dto;

import java.time.Instant;

/**
 * Resumen de un grupo para listados.
 */
public record GroupSummaryDto(
        Long id,
        String name,
        String description,
        int memberCount,
        Instant createdAt) {
}
