package com.sgvi.sistema_ventas.controller;

import com.sgvi.sistema_ventas.model.dto.auth.LoginRequestDTO;
import com.sgvi.sistema_ventas.model.dto.auth.LoginResponse;
import com.sgvi.sistema_ventas.model.dto.auth.MessageResponse;
import com.sgvi.sistema_ventas.model.dto.auth.RegisterRequestDTO;
import com.sgvi.sistema_ventas.security.UserPrincipal;
import com.sgvi.sistema_ventas.security.jwt.JwtTokenProvider;
import com.sgvi.sistema_ventas.service.interfaces.IUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de autenticación y autorización.
 * Proporciona endpoints públicos para login, registro, validación de tokens y logout.
 * Implementa requisito RF-001 del SRS.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints para autenticación y gestión de tokens JWT")
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final IUsuarioService usuarioService;
    private final UserDetailsService userDetailsService;

    /**
     * RF-001: Autentica usuario y genera token JWT.
     * Valida credenciales contra base de datos y retorna token con validez de 24 horas.
     * Después de 3 intentos fallidos, la cuenta se bloquea por 30 minutos.
     *
     * @param loginRequest DTO con username y password del usuario
     * @return ResponseEntity con LoginResponse conteniendo token JWT y datos del usuario,
     *         o MessageResponse con error en caso de fallo
     */
    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica usuario con username/email y contraseña. Retorna token JWT válido por 24 horas"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuario deshabilitado o bloqueado",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            log.info("Intento de login para usuario: {}", loginRequest.getUsername());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken(authentication);
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

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

            log.info("Login exitoso para usuario: {} - Rol: {}",
                    loginRequest.getUsername(),
                    userPrincipal.getNombreRol());

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("Credenciales inválidas para usuario: {}", loginRequest.getUsername());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Usuario o contraseña incorrectos"));

        } catch (DisabledException e) {
            log.warn("Intento de acceso con usuario deshabilitado: {}", loginRequest.getUsername());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Usuario deshabilitado. Contacte al administrador"));

        } catch (Exception e) {
            log.error("Error inesperado durante login para usuario: {} - Error: {}",
                    loginRequest.getUsername(),
                    e.getMessage(),
                    e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error en el servidor. Intente nuevamente"));
        }
    }

    /**
     * RF-001: Registra nuevo usuario en el sistema.
     * Solo crea usuarios con rol básico. Administrador debe asignar roles específicos posteriormente.
     *
     * @param registerRequest DTO con datos del nuevo usuario
     * @return ResponseEntity con mensaje de éxito o error
     */
    @PostMapping("/register")
    @Operation(
            summary = "Registrar usuario",
            description = "Crea nuevo usuario en el sistema con rol básico por defecto"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Usuario o email ya existe",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        try {
            log.info("Intento de registro para usuario: {}", registerRequest.getUsername());

            if (usuarioService.existeUsername(registerRequest.getUsername())) {
                log.warn("Intento de registro con username existente: {}", registerRequest.getUsername());
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(new MessageResponse("El nombre de usuario ya existe"));
            }

            if (usuarioService.existeCorreo(registerRequest.getEmail())) {
                log.warn("Intento de registro con email existente: {}", registerRequest.getEmail());
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(new MessageResponse("El email ya está registrado"));
            }

            usuarioService.registrarUsuario(registerRequest);

            log.info("Usuario registrado exitosamente: {}", registerRequest.getUsername());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new MessageResponse("Usuario registrado exitosamente"));

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos durante registro: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al registrar usuario: {} - Error: {}",
                    registerRequest.getUsername(),
                    e.getMessage(),
                    e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al registrar usuario. Intente nuevamente"));
        }
    }

    /**
     * Valida si el token JWT proporcionado es válido.
     * Verifica firma, expiración y formato del token.
     *
     * @param authHeader Header de autorización con formato "Bearer {token}"
     * @return ResponseEntity con resultado de validación
     */
    @GetMapping("/validate")
    @Operation(
            summary = "Validar token",
            description = "Verifica si el token JWT es válido (firma, expiración, formato)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token válido",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Token no proporcionado o formato inválido",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido o expirado",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<MessageResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Intento de validación sin token o formato inválido");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("Token no proporcionado o formato inválido"));
            }

            String jwt = authHeader.substring(7);

            if (jwtTokenProvider.validateToken(jwt)) {
                String username = jwtTokenProvider.getUsernameFromToken(jwt);
                log.info("Token validado exitosamente para usuario: {}", username);
                return ResponseEntity.ok(
                        new MessageResponse("Token válido para usuario: " + username)
                );
            } else {
                log.warn("Token inválido o expirado");
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Token inválido o expirado"));
            }

        } catch (Exception e) {
            log.error("Error al validar token: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Error al validar token"));
        }
    }

    /**
     * Renueva token JWT existente.
     * Genera nuevo token con validez de 24 horas usando token actual válido.
     *
     * @param authHeader Header de autorización con token actual
     * @return ResponseEntity con nuevo token o error
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Renovar token",
            description = "Genera nuevo token JWT usando token actual válido. Nueva validez: 24 horas"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token renovado exitosamente",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Token no proporcionado",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido o expirado",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error al renovar token",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Intento de refresh sin token");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("Token no proporcionado"));
            }

            String oldToken = authHeader.substring(7);

            if (!jwtTokenProvider.validateToken(oldToken)) {
                log.warn("Intento de refresh con token inválido");
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Token inválido o expirado"));
            }

            String username = jwtTokenProvider.getUsernameFromToken(oldToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

            String newToken = jwtTokenProvider.generateToken(authentication);

            LoginResponse response = LoginResponse.builder()
                    .token(newToken)
                    .type("Bearer")
                    .build();

            log.info("Token renovado exitosamente para usuario: {}", username);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al renovar token: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al renovar token"));
        }
    }

    /**
     * Cierra sesión del usuario actual.
     * Limpia el contexto de seguridad. Nota: JWT es stateless,
     * el cliente debe descartar el token.
     *
     * @return ResponseEntity con mensaje de confirmación
     */
    @PostMapping("/logout")
    @Operation(
            summary = "Cerrar sesión",
            description = "Limpia el contexto de seguridad del servidor. El cliente debe eliminar el token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Sesión cerrada exitosamente",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<MessageResponse> logout() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                log.info("Logout exitoso para usuario: {}", userPrincipal.getUsername());
            }

            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(new MessageResponse("Sesión cerrada exitosamente"));

        } catch (Exception e) {
            log.error("Error durante logout: {}", e.getMessage());
            return ResponseEntity.ok(new MessageResponse("Sesión cerrada"));
        }
    }
}