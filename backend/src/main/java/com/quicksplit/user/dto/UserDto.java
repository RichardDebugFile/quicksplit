package com.quicksplit.user.dto;

import com.quicksplit.user.User;

/**
 * Representacion publica de un usuario (nunca incluye el hash de la contrasena).
 */
public record UserDto(Long id, String name, String email) {

    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }
}
