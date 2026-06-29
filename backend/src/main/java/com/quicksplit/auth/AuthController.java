package com.quicksplit.auth;

import com.quicksplit.auth.dto.AuthResponse;
import com.quicksplit.auth.dto.LoginRequest;
import com.quicksplit.auth.dto.RegisterRequest;
import com.quicksplit.common.NotFoundException;
import com.quicksplit.security.AppUserPrincipal;
import com.quicksplit.user.UserRepository;
import com.quicksplit.user.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de autenticacion: registro, login y usuario actual.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Registro, login y perfil del usuario actual")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar un nuevo usuario y devolver un token JWT")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion y devolver un token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener los datos del usuario autenticado")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal AppUserPrincipal principal) {
        UserDto user = userRepository.findById(principal.getId())
                .map(UserDto::from)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return ResponseEntity.ok(user);
    }
}
