package com.quicksplit.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Genera y valida tokens JWT (HS256) usados para autenticar las peticiones.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${quicksplit.jwt.secret}") String secret,
            @Value("${quicksplit.jwt.expiration-ms}") long expirationMs) {
        // HS256 exige una clave de al menos 256 bits (32 bytes).
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                "quicksplit.jwt.secret debe tener al menos 32 caracteres (256 bits) para HS256");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMs = expirationMs;
    }

    /** Crea un token firmado para el usuario indicado. */
    public String generateToken(Long userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(email)
                .claim("uid", userId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /** Devuelve el email (subject) si el token es valido; lanza excepcion si no lo es. */
    public String extractEmail(String token) {
        return parse(token).getSubject();
    }

    /** Devuelve el id de usuario almacenado en el claim {@code uid}. */
    public Long extractUserId(String token) {
        return parse(token).get("uid", Long.class);
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
