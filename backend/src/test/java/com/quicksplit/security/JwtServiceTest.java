package com.quicksplit.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de generacion y validacion de tokens JWT.
 */
class JwtServiceTest {

    private static final String SECRET = "test-secret-0123456789abcdef0123456789abcdef-extra";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3_600_000L);
    }

    @Test
    @DisplayName("El token generado conserva email e id de usuario")
    void tokenCarriesEmailAndUserId() {
        String token = jwtService.generateToken(42L, "ana@example.com");

        assertThat(jwtService.extractEmail(token)).isEqualTo("ana@example.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("Un token manipulado es rechazado")
    void tamperedTokenIsRejected() {
        String token = jwtService.generateToken(1L, "x@example.com");
        String tampered = token.substring(0, token.length() - 2) + "ab";

        assertThatThrownBy(() -> jwtService.extractEmail(tampered))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Un token expirado es rechazado")
    void expiredTokenIsRejected() {
        JwtService shortLived = new JwtService(SECRET, -1_000L); // ya expirado
        String token = shortLived.generateToken(1L, "x@example.com");

        assertThatThrownBy(() -> shortLived.extractEmail(token))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Un secreto demasiado corto no es aceptado")
    void shortSecretIsRejected() {
        assertThatThrownBy(() -> new JwtService("corto", 1000L))
                .isInstanceOf(IllegalStateException.class);
    }
}
