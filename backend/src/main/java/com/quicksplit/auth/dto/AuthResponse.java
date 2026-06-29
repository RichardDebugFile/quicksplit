package com.quicksplit.auth.dto;

import com.quicksplit.user.dto.UserDto;

/**
 * Respuesta de autenticacion: token JWT y datos del usuario.
 */
public record AuthResponse(String token, String tokenType, UserDto user) {

    public static AuthResponse bearer(String token, UserDto user) {
        return new AuthResponse(token, "Bearer", user);
    }
}
