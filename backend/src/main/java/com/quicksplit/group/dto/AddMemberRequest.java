package com.quicksplit.group.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Agrega un usuario existente al grupo mediante su email.
 */
public record AddMemberRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no es valido")
        String email) {
}
