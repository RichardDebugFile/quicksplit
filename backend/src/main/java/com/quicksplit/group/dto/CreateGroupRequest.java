package com.quicksplit.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear un grupo de gastos.
 */
public record CreateGroupRequest(
        @NotBlank(message = "El nombre del grupo es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String name,

        @Size(max = 280, message = "La descripcion es demasiado larga")
        String description) {
}
