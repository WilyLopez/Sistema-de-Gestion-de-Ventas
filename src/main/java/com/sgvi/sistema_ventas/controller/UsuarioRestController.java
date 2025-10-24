package com.sgvi.sistema_ventas.controller;

import com.sgvi.sistema_ventas.model.dto.auth.MessageResponse;
import com.sgvi.sistema_ventas.model.dto.usuario.CambiarContrasenaRequest;
import com.sgvi.sistema_ventas.model.dto.usuario.UsuarioCreateDTO;
import com.sgvi.sistema_ventas.model.dto.usuario.UsuarioUpdateDTO;
import com.sgvi.sistema_ventas.model.entity.Usuario;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para gestión de usuarios.
 * Implementa requisito RF-003 del SRS: Gestión de Usuarios.
 * Solo accesible por usuarios con rol ADMINISTRADOR.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuarios", description = "Endpoints para gestión de usuarios del sistema (solo Administrador)")
public class UsuarioRestController {

    private final IUsuarioService usuarioService;

    /**
     * RF-003: Crear nuevo usuario en el sistema.
     * Solo el administrador puede crear usuarios.
     *
     * @param createDTO DTO con datos del nuevo usuario
     * @return Usuario creado
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Crear usuario",
            description = "Crea un nuevo usuario en el sistema. Solo administrador. Valida username y correo únicos"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Usuario.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o username/correo duplicado",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos (solo administrador)"
            )
    })
    public ResponseEntity<?> crear(@Valid @RequestBody UsuarioCreateDTO createDTO) {
        try {
            log.info("POST /api/usuarios - Crear usuario: {}", createDTO.getUsername());

            if (usuarioService.existeUsername(createDTO.getUsername())) {
                log.warn("Intento de crear usuario con username existente: {}", createDTO.getUsername());
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El username ya existe: " + createDTO.getUsername()));
            }

            if (usuarioService.existeCorreo(createDTO.getCorreo())) {
                log.warn("Intento de crear usuario con correo existente: {}", createDTO.getCorreo());
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El correo ya está registrado: " + createDTO.getCorreo()));
            }

            Usuario usuario = convertirAUsuario(createDTO);
            Usuario usuarioCreado = usuarioService.crear(usuario);

            log.info("Usuario creado exitosamente: {} - ID: {}", usuarioCreado.getUsername(), usuarioCreado.getIdUsuario());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(usuarioCreado);

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al crear usuario: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al crear usuario: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al crear usuario. Intente nuevamente"));
        }
    }

    /**
     * RF-003: Actualizar datos de un usuario existente.
     * Permite actualización parcial de campos.
     *
     * @param id ID del usuario
     * @param updateDTO DTO con datos a actualizar
     * @return Usuario actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Actualizar usuario",
            description = "Actualiza datos de un usuario. Permite actualización parcial. Solo administrador"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o correo duplicado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO updateDTO) {

        try {
            log.info("PUT /api/usuarios/{} - Actualizar usuario", id);

            Usuario usuarioExistente = usuarioService.obtenerPorId(id);

            if (updateDTO.getCorreo() != null &&
                    !usuarioExistente.getCorreo().equals(updateDTO.getCorreo()) &&
                    usuarioService.existeCorreo(updateDTO.getCorreo())) {

                log.warn("Intento de actualizar con correo existente: {}", updateDTO.getCorreo());
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El correo ya está registrado: " + updateDTO.getCorreo()));
            }

            aplicarActualizacion(usuarioExistente, updateDTO);

            Usuario usuarioActualizado = usuarioService.actualizar(id, usuarioExistente);

            log.info("Usuario actualizado exitosamente: {}", id);

            return ResponseEntity.ok(usuarioActualizado);

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al actualizar usuario {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al actualizar usuario {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Usuario no encontrado con ID: " + id));
        }
    }

    /**
     * RF-003: Desactivar usuario (soft delete).
     * Marca el usuario como inactivo sin eliminarlo físicamente.
     *
     * @param id ID del usuario
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Desactivar usuario",
            description = "Desactiva un usuario del sistema (soft delete). No puede desactivarse a sí mismo"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuario desactivado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No puede desactivarse a sí mismo"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    public ResponseEntity<?> desactivar(@PathVariable Long id) {
        try {
            log.info("DELETE /api/usuarios/{} - Desactivar usuario", id);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            Usuario usuarioActual = usuarioService.obtenerPorUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("Usuario actual no encontrado"));

            if (usuarioActual.getIdUsuario().equals(id)) {
                log.warn("Intento de desactivarse a sí mismo: {}", currentUsername);
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("No puede desactivarse a sí mismo"));
            }

            usuarioService.desactivar(id);

            log.info("Usuario desactivado exitosamente: {}", id);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error al desactivar usuario {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Usuario no encontrado con ID: " + id));
        }
    }

    /**
     * RF-003: Activar usuario previamente desactivado.
     *
     * @param id ID del usuario
     * @return Respuesta sin contenido
     */
    @PutMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Activar usuario",
            description = "Activa un usuario previamente desactivado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuario activado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    public ResponseEntity<?> activar(@PathVariable Long id) {
        try {
            log.info("PUT /api/usuarios/{}/activar", id);

            usuarioService.activar(id);

            log.info("Usuario activado exitosamente: {}", id);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error al activar usuario {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Usuario no encontrado con ID: " + id));
        }
    }

    /**
     * RF-003: Obtener usuario por ID.
     *
     * @param id ID del usuario
     * @return Usuario encontrado
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Obtener usuario por ID",
            description = "Obtiene los datos completos de un usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            log.info("GET /api/usuarios/{}", id);
            Usuario usuario = usuarioService.obtenerPorId(id);
            return ResponseEntity.ok(usuario);

        } catch (Exception e) {
            log.error("Error al obtener usuario {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Usuario no encontrado con ID: " + id));
        }
    }

    /**
     * RF-003: Listar todos los usuarios con paginación.
     *
     * @param pageable Parámetros de paginación
     * @return Página de usuarios
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Listar usuarios",
            description = "Lista todos los usuarios con paginación. Ejemplo: ?page=0&size=20&sort=nombre,asc"
    )
    public ResponseEntity<Page<Usuario>> listarTodos(Pageable pageable) {
        log.info("GET /api/usuarios - Listar usuarios (página: {})", pageable.getPageNumber());
        Page<Usuario> usuarios = usuarioService.listarTodos(pageable);
        return ResponseEntity.ok(usuarios);
    }

    /**
     * RF-003: Listar usuarios por estado.
     *
     * @param estado Estado a filtrar (true=Activo, false=Inactivo)
     * @param pageable Parámetros de paginación
     * @return Página de usuarios filtrados
     */
    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Filtrar por estado",
            description = "Lista usuarios activos o inactivos"
    )
    public ResponseEntity<Page<Usuario>> listarPorEstado(
            @PathVariable Boolean estado,
            Pageable pageable) {

        log.info("GET /api/usuarios/estado/{}", estado);
        Page<Usuario> usuarios = usuarioService.listarPorEstado(estado, pageable);
        return ResponseEntity.ok(usuarios);
    }

    /**
     * RF-003: Buscar usuarios por nombre o apellido.
     *
     * @param nombre Texto a buscar
     * @param pageable Parámetros de paginación
     * @return Página de usuarios que coinciden
     */
    @GetMapping("/buscar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Buscar usuarios",
            description = "Busca usuarios por nombre o apellido"
    )
    public ResponseEntity<Page<Usuario>> buscarPorNombre(
            @RequestParam String nombre,
            Pageable pageable) {

        log.info("GET /api/usuarios/buscar?nombre={}", nombre);
        Page<Usuario> usuarios = usuarioService.buscarPorNombre(nombre, pageable);
        return ResponseEntity.ok(usuarios);
    }

    /**
     * RF-001: Cambiar contraseña de usuario.
     * Verifica contraseña actual antes de cambiar.
     * Cualquier usuario autenticado puede cambiar su propia contraseña.
     *
     * @param id ID del usuario
     * @param request DTO con contraseñas
     * @return Mensaje de confirmación
     */
    @PutMapping("/{id}/cambiar-contrasena")
    @Operation(
            summary = "Cambiar contraseña",
            description = "Cambia la contraseña de un usuario. Verifica contraseña actual"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contraseña cambiada exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Contraseña actual incorrecta o contraseñas no coinciden"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Solo puede cambiar su propia contraseña"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    public ResponseEntity<?> cambiarContrasena(
            @PathVariable Long id,
            @Valid @RequestBody CambiarContrasenaRequest request) {

        try {
            log.info("PUT /api/usuarios/{}/cambiar-contrasena", id);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            Usuario usuarioActual = usuarioService.obtenerPorUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("Usuario actual no encontrado"));

            boolean esAdministrador = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

            if (!esAdministrador && !usuarioActual.getIdUsuario().equals(id)) {
                log.warn("Usuario {} intentó cambiar contraseña de otro usuario", currentUsername);
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Solo puede cambiar su propia contraseña"));
            }

            if (!request.getContrasenaNueva().equals(request.getConfirmarContrasena())) {
                log.warn("Contraseñas no coinciden para usuario {}", id);
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("Las contraseñas no coinciden"));
            }

            usuarioService.cambiarContrasena(id, request.getContrasenaActual(), request.getContrasenaNueva());

            log.info("Contraseña cambiada exitosamente para usuario {}", id);

            return ResponseEntity.ok(
                    Map.of("mensaje", "Contraseña actualizada exitosamente")
            );

        } catch (IllegalArgumentException e) {
            log.warn("Contraseña actual incorrecta para usuario {}", id);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Contraseña actual incorrecta"));

        } catch (Exception e) {
            log.error("Error al cambiar contraseña del usuario {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al cambiar contraseña"));
        }
    }

    /**
     * Obtener perfil del usuario actual autenticado.
     * Cualquier usuario autenticado puede consultar su propio perfil.
     *
     * @return Usuario actual
     */
    @GetMapping("/perfil")
    @Operation(
            summary = "Obtener perfil actual",
            description = "Obtiene los datos del usuario autenticado actualmente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil obtenido exitosamente"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            )
    })
    public ResponseEntity<?> obtenerPerfil() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            log.info("GET /api/usuarios/perfil - Usuario: {}", username);

            Usuario usuario = usuarioService.obtenerPorUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            usuarioService.registrarLogin(usuario.getIdUsuario(), LocalDateTime.now());

            return ResponseEntity.ok(usuario);

        } catch (Exception e) {
            log.error("Error al obtener perfil: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener perfil"));
        }
    }

    /**
     * Obtener estadísticas de usuarios.
     * Solo administrador.
     *
     * @return Estadísticas (total, activos, inactivos, por rol)
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Estadísticas de usuarios",
            description = "Obtiene estadísticas generales de usuarios del sistema"
    )
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        log.info("GET /api/usuarios/estadisticas");

        Page<Usuario> todosUsuarios = usuarioService.listarTodos(Pageable.unpaged());
        Page<Usuario> activos = usuarioService.listarPorEstado(true, Pageable.unpaged());
        Page<Usuario> inactivos = usuarioService.listarPorEstado(false, Pageable.unpaged());

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalUsuarios", todosUsuarios.getTotalElements());
        estadisticas.put("usuariosActivos", activos.getTotalElements());
        estadisticas.put("usuariosInactivos", inactivos.getTotalElements());

        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Convierte UsuarioCreateDTO a entidad Usuario.
     *
     * @param createDTO DTO de creación
     * @return Entidad Usuario
     */
    private Usuario convertirAUsuario(UsuarioCreateDTO createDTO) {
        Usuario usuario = new Usuario();
        usuario.setUsername(createDTO.getUsername());
        usuario.setNombre(createDTO.getNombre());
        usuario.setApellido(createDTO.getApellido());
        usuario.setCorreo(createDTO.getCorreo());
        usuario.setContrasena(createDTO.getContrasena());
        usuario.setTelefono(createDTO.getTelefono());
        usuario.setDireccion(createDTO.getDireccion());
        usuario.setIdRol(createDTO.getIdRol());
        usuario.setEstado(createDTO.getEstado() != null ? createDTO.getEstado() : true);
        return usuario;
    }

    /**
     * Aplica actualización parcial de UsuarioUpdateDTO a entidad Usuario.
     *
     * @param usuario Usuario existente
     * @param updateDTO DTO con campos a actualizar
     */
    private void aplicarActualizacion(Usuario usuario, UsuarioUpdateDTO updateDTO) {
        if (updateDTO.getNombre() != null) {
            usuario.setNombre(updateDTO.getNombre());
        }
        if (updateDTO.getApellido() != null) {
            usuario.setApellido(updateDTO.getApellido());
        }
        if (updateDTO.getCorreo() != null) {
            usuario.setCorreo(updateDTO.getCorreo());
        }
        if (updateDTO.getTelefono() != null) {
            usuario.setTelefono(updateDTO.getTelefono());
        }
        if (updateDTO.getDireccion() != null) {
            usuario.setDireccion(updateDTO.getDireccion());
        }
        if (updateDTO.getIdRol() != null) {
            usuario.setIdRol(updateDTO.getIdRol());
        }
        if (updateDTO.getEstado() != null) {
            usuario.setEstado(updateDTO.getEstado());
        }
    }
}