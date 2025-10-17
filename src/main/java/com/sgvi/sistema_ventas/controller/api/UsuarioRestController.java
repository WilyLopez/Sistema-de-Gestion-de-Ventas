package com.sgvi.sistema_ventas.controller.api;

import com.sgvi.sistema_ventas.model.entity.Usuario;
import com.sgvi.sistema_ventas.service.interfaces.IUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST para gestión de usuarios.
 * RF-003: Gestión de Usuarios
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMINISTRADOR')")
@Tag(name = "Usuarios", description = "Endpoints para gestión de usuarios (solo Admin)")
public class UsuarioRestController {

    private final IUsuarioService usuarioService;

    /**
     * RF-003: Crear usuario
     * POST /api/usuarios
     */
    @PostMapping
    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario en el sistema")
    public ResponseEntity<Usuario> crear(@Valid @RequestBody Usuario usuario) {
        log.info("POST /api/usuarios - Crear usuario");
        Usuario usuarioCreado = usuarioService.crear(usuario);
        return new ResponseEntity<>(usuarioCreado, HttpStatus.CREATED);
    }

    /**
     * RF-003: Actualizar usuario
     * PUT /api/usuarios/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza datos de un usuario")
    public ResponseEntity<Usuario> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Usuario usuario) {
        log.info("PUT /api/usuarios/{}", id);
        Usuario usuarioActualizado = usuarioService.actualizar(id, usuario);
        return ResponseEntity.ok(usuarioActualizado);
    }

    /**
     * RF-003: Desactivar usuario
     * DELETE /api/usuarios/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar usuario", description = "Desactiva un usuario del sistema")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        log.info("DELETE /api/usuarios/{}", id);
        usuarioService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * RF-003: Obtener usuario por ID
     * GET /api/usuarios/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario", description = "Obtiene un usuario por ID")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/usuarios/{}", id);
        Usuario usuario = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(usuario);
    }

    /**
     * RF-003: Listar usuarios
     * GET /api/usuarios
     */
    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Lista todos los usuarios con paginación")
    public ResponseEntity<Page<Usuario>> listarTodos(Pageable pageable) {
        log.info("GET /api/usuarios");
        Page<Usuario> usuarios = usuarioService.listarTodos(pageable);
        return ResponseEntity.ok(usuarios);
    }

    /**
     * RF-001: Cambiar contraseña
     * PUT /api/usuarios/{id}/cambiar-contrasena
     */
    @PutMapping("/{id}/cambiar-contrasena")
    @Operation(summary = "Cambiar contraseña", description = "Cambia la contraseña de un usuario")
    public ResponseEntity<Map<String, String>> cambiarContrasena(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwords) {
        log.info("PUT /api/usuarios/{}/cambiar-contrasena", id);

        String contrasenaActual = passwords.get("contrasenaActual");
        String contrasenaNueva = passwords.get("contrasenaNueva");

        usuarioService.cambiarContrasena(id, contrasenaActual, contrasenaNueva);

        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada exitosamente"));
    }

    /**
     * RF-003: Activar usuario
     * PUT /api/usuarios/{id}/activar
     */
    @PutMapping("/{id}/activar")
    @Operation(summary = "Activar usuario", description = "Activa un usuario previamente desactivado")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        log.info("PUT /api/usuarios/{}/activar", id);
        usuarioService.activar(id);
        return ResponseEntity.noContent().build();
    }
}
