package com.quicksplit.auth;

import com.quicksplit.auth.dto.AuthResponse;
import com.quicksplit.auth.dto.LoginRequest;
import com.quicksplit.auth.dto.RegisterRequest;
import com.quicksplit.common.ConflictException;
import com.quicksplit.security.JwtService;
import com.quicksplit.user.User;
import com.quicksplit.user.UserRepository;
import com.quicksplit.user.dto.UserDto;
import java.time.Instant;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logica de registro e inicio de sesion.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Ya existe una cuenta con ese email");
        }

        User user = User.builder()
                .name(request.name().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .createdAt(Instant.now())
                .build();
        User saved = userRepository.save(user);

        String token = jwtService.generateToken(saved.getId(), saved.getEmail());
        return AuthResponse.bearer(token, UserDto.from(saved));
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        // Lanza AuthenticationException (401) si las credenciales son invalidas.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));

        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return AuthResponse.bearer(token, UserDto.from(user));
    }
}
