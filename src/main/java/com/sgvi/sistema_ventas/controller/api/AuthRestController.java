package com.sgvi.sistema_ventas.controller.api;

import com.sgvi.sistema_ventas.model.dto.auth.LoginRequestDTO;
import com.sgvi.sistema_ventas.model.dto.auth.LoginResponse;
import com.sgvi.sistema_ventas.model.dto.auth.MessageResponse;
import com.sgvi.sistema_ventas.security.UserPrincipal;
import com.sgvi.sistema_ventas.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para autenticación y autorización.
 * Endpoints públicos para login.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints para autenticación y gestión de tokens")
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * RF-001: Endpoint de login
     * POST /api/auth/login
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica usuario y devuelve token JWT")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Intento de login para usuario: {}", loginRequest.getUsername());

        // Autenticar con Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // Establecer autenticación en el contexto
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generar token JWT
        String jwt = jwtTokenProvider.generateToken(authentication);

        // Obtener detalles del usuario
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Construir respuesta
        LoginResponse response = LoginResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .nombre(userPrincipal.getNombre())
                .apellido(userPrincipal.getApellido())
                .email(userPrincipal.getEmail())
                .rol(userPrincipal.getNombreRol())
                .build();

        log.info("Login exitoso para usuario: {}", loginRequest.getUsername());

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para verificar si el token es válido
     * GET /api/auth/validate
     */
    @GetMapping("/validate")
    @Operation(summary = "Validar token", description = "Verifica si el token JWT es válido")
    public ResponseEntity<MessageResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String jwt = authHeader.substring(7); // Remover "Bearer "

            if (jwtTokenProvider.validateToken(jwt)) {
                String username = jwtTokenProvider.getUsernameFromToken(jwt);
                return ResponseEntity.ok(new MessageResponse("Token válido para usuario: " + username));
            } else {
                return ResponseEntity.status(401).body(new MessageResponse("Token inválido"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new MessageResponse("Error al validar token"));
        }
    }

    /**
     * Endpoint de logout (opcional - JWT es stateless)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Limpia el contexto de seguridad")
    public ResponseEntity<MessageResponse> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("Sesión cerrada exitosamente"));
    }
}
