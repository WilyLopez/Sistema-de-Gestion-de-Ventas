package com.sgvi.sistema_ventas.controller;

import com.sgvi.sistema_ventas.model.dto.auth.MessageResponse;
import com.sgvi.sistema_ventas.model.dto.rol.AsignarPermisosRequest;
import com.sgvi.sistema_ventas.model.entity.Permiso;
import com.sgvi.sistema_ventas.model.entity.Rol;
import com.sgvi.sistema_ventas.service.interfaces.IRolService;
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
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controlador REST para gestión de roles y permisos.
 * Implementa requisito RF-002 del SRS: Gestión de Roles y Permisos.
 * Solo accesible por usuarios con rol ADMINISTRADOR.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMINISTRADOR')")
@Tag(name = "Roles", description = "Endpoints para gestión de roles y permisos (solo Administrador)")
public class RolRestController {

    private final IRolService rolService;

    /**
     * RF-002: Crear nuevo rol en el sistema.
     * Valida nombre único y nivel de acceso válido.
     *
     * @param rol Datos del rol
     * @return Rol creado
     */
    @PostMapping
    @Operation(
            summary = "Crear rol",
            description = "Crea un nuevo rol en el sistema. Valida nombre único y nivel de acceso (1-10)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Rol creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Rol.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o nombre duplicado",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos (solo administrador)"
            )
    })
    public ResponseEntity<?> crear(@Valid @RequestBody Rol rol) {
        try {
            log.info("POST /api/roles - Crear rol: {}", rol.getNombre());

            if (rolService.existePorNombre(rol.getNombre())) {
                log.warn("Intento de crear rol con nombre duplicado: {}", rol.getNombre());
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El nombre del rol ya existe: " + rol.getNombre()));
            }

            if (rol.getNivelAcceso() == null || rol.getNivelAcceso() < 1 || rol.getNivelAcceso() > 10) {
                log.warn("Intento de crear rol con nivel de acceso inválido: {}", rol.getNivelAcceso());
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El nivel de acceso debe estar entre 1 y 10"));
            }

            Rol rolCreado = rolService.crear(rol);

            log.info("Rol creado exitosamente: {} - ID: {}", rolCreado.getNombre(), rolCreado.getIdRol());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(rolCreado);

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al crear rol: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al crear rol: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al crear rol. Intente nuevamente"));
        }
    }

    /**
     * RF-002: Actualizar rol existente.
     * No permite modificar roles del sistema (administrador, vendedor, empleado).
     *
     * @param id ID del rol
     * @param rol Datos actualizados
     * @return Rol actualizado
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar rol",
            description = "Actualiza datos de un rol existente. No permite modificar roles del sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Rol actualizado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos, nombre duplicado o rol del sistema"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rol no encontrado"
            )
    })
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Rol rol) {

        try {
            log.info("PUT /api/roles/{} - Actualizar rol", id);

            Rol rolExistente = rolService.obtenerPorId(id);

            String[] rolesSistema = {"administrador", "vendedor", "empleado"};
            for (String rolSistema : rolesSistema) {
                if (rolExistente.getNombre().equalsIgnoreCase(rolSistema)) {
                    log.warn("Intento de modificar rol del sistema: {}", rolExistente.getNombre());
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("No se pueden modificar roles del sistema"));
                }
            }

            if (!rolExistente.getNombre().equals(rol.getNombre()) &&
                    rolService.existePorNombre(rol.getNombre())) {

                log.warn("Intento de actualizar con nombre duplicado: {}", rol.getNombre());
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El nombre del rol ya existe: " + rol.getNombre()));
            }

            if (rol.getNivelAcceso() != null && (rol.getNivelAcceso() < 1 || rol.getNivelAcceso() > 10)) {
                log.warn("Nivel de acceso inválido: {}", rol.getNivelAcceso());
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El nivel de acceso debe estar entre 1 y 10"));
            }

            Rol rolActualizado = rolService.actualizar(id, rol);

            log.info("Rol actualizado exitosamente: {}", id);

            return ResponseEntity.ok(rolActualizado);

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al actualizar rol {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al actualizar rol {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Rol no encontrado con ID: " + id));
        }
    }

    /**
     * RF-002: Eliminar rol (soft delete).
     * No permite eliminar roles del sistema ni roles con usuarios asignados.
     *
     * @param id ID del rol
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar rol",
            description = "Desactiva un rol (soft delete). No permite eliminar roles del sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Rol eliminado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No se puede eliminar rol del sistema",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rol no encontrado"
            )
    })
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            log.info("DELETE /api/roles/{} - Eliminar rol", id);

            Rol rol = rolService.obtenerPorId(id);

            String[] rolesSistema = {"administrador", "vendedor", "empleado"};
            for (String rolSistema : rolesSistema) {
                if (rol.getNombre().equalsIgnoreCase(rolSistema)) {
                    log.warn("Intento de eliminar rol del sistema: {}", rol.getNombre());
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("No se pueden eliminar roles del sistema"));
                }
            }

            rolService.eliminar(id);

            log.info("Rol eliminado exitosamente: {}", id);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error al eliminar rol {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Rol no encontrado con ID: " + id));
        }
    }

    /**
     * RF-002: Obtener rol por ID con sus permisos.
     *
     * @param id ID del rol
     * @return Rol encontrado
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener rol por ID",
            description = "Obtiene los datos completos de un rol incluyendo sus permisos"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Rol encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rol no encontrado"
            )
    })
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            log.info("GET /api/roles/{} - Obtener rol", id);
            Rol rol = rolService.obtenerPorId(id);
            return ResponseEntity.ok(rol);

        } catch (Exception e) {
            log.error("Error al obtener rol {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Rol no encontrado con ID: " + id));
        }
    }

    /**
     * RF-002: Obtener rol por nombre.
     *
     * @param nombre Nombre del rol
     * @return Rol encontrado
     */
    @GetMapping("/nombre/{nombre}")
    @Operation(
            summary = "Buscar por nombre",
            description = "Obtiene un rol por su nombre único"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Rol encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rol no encontrado con ese nombre"
            )
    })
    public ResponseEntity<?> obtenerPorNombre(@PathVariable String nombre) {
        try {
            log.info("GET /api/roles/nombre/{} - Buscar rol", nombre);
            Rol rol = rolService.obtenerPorNombre(nombre);
            return ResponseEntity.ok(rol);

        } catch (Exception e) {
            log.error("Error al buscar rol por nombre {}: {}", nombre, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Rol no encontrado: " + nombre));
        }
    }

    /**
     * RF-002: Listar todos los roles con paginación.
     *
     * @param pageable Parámetros de paginación
     * @return Página de roles
     */
    @GetMapping
    @Operation(
            summary = "Listar roles",
            description = "Lista todos los roles con paginación. Ejemplo: ?page=0&size=20&sort=nombre,asc"
    )
    public ResponseEntity<Page<Rol>> listarTodos(Pageable pageable) {
        log.info("GET /api/roles - Listar roles (página: {})", pageable.getPageNumber());
        Page<Rol> roles = rolService.listarTodos(pageable);
        return ResponseEntity.ok(roles);
    }

    /**
     * RF-002: Listar solo roles activos.
     * Útil para selects/dropdowns en formularios.
     *
     * @return Lista de roles activos
     */
    @GetMapping("/activos")
    @Operation(
            summary = "Roles activos",
            description = "Lista solo roles activos ordenados por nombre. Útil para asignación de usuarios"
    )
    public ResponseEntity<List<Rol>> listarActivos() {
        log.info("GET /api/roles/activos - Listar roles activos");
        List<Rol> roles = rolService.listarActivos();
        return ResponseEntity.ok(roles);
    }

    /**
     * RF-002: Asignar permisos a un rol.
     * Reemplaza todos los permisos actuales con la nueva lista.
     *
     * @param id ID del rol
     * @param request DTO con lista de IDs de permisos
     * @return Mensaje de confirmación
     */
    @PostMapping("/{id}/permisos")
    @Operation(
            summary = "Asignar permisos",
            description = "Asigna una lista de permisos a un rol. Reemplaza permisos actuales"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Permisos asignados exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Lista de permisos vacía o IDs inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rol no encontrado"
            )
    })
    public ResponseEntity<?> asignarPermisos(
            @PathVariable Long id,
            @Valid @RequestBody AsignarPermisosRequest request) {

        try {
            log.info("POST /api/roles/{}/permisos - Asignar {} permisos",
                    id,
                    request.getIdsPermisos().size());

            rolService.asignarPermisos(id, request.getIdsPermisos());

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Permisos asignados exitosamente");
            respuesta.put("rolId", id);
            respuesta.put("cantidadPermisos", request.getIdsPermisos().size());

            log.info("Permisos asignados exitosamente al rol {}", id);

            return ResponseEntity.ok(respuesta);

        } catch (IllegalArgumentException e) {
            log.warn("IDs de permisos inválidos para rol {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al asignar permisos al rol {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Rol no encontrado con ID: " + id));
        }
    }

    /**
     * RF-002: Remover un permiso específico de un rol.
     *
     * @param id ID del rol
     * @param idPermiso ID del permiso a remover
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}/permisos/{idPermiso}")
    @Operation(
            summary = "Remover permiso",
            description = "Elimina un permiso específico de un rol"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Permiso removido exitosamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rol o permiso no encontrado"
            )
    })
    public ResponseEntity<?> removerPermiso(
            @PathVariable Long id,
            @PathVariable Long idPermiso) {

        try {
            log.info("DELETE /api/roles/{}/permisos/{} - Remover permiso", id, idPermiso);

            rolService.removerPermiso(id, idPermiso);

            log.info("Permiso {} removido del rol {}", idPermiso, id);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error al remover permiso {} del rol {}: {}", idPermiso, id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Rol o permiso no encontrado"));
        }
    }

    /**
     * RF-002: Obtener todos los permisos asignados a un rol.
     *
     * @param id ID del rol
     * @return Set de permisos del rol
     */
    @GetMapping("/{id}/permisos")
    @Operation(
            summary = "Obtener permisos de rol",
            description = "Lista todos los permisos asignados a un rol específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de permisos obtenida"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rol no encontrado"
            )
    })
    public ResponseEntity<?> obtenerPermisos(@PathVariable Long id) {
        try {
            log.info("GET /api/roles/{}/permisos - Obtener permisos del rol", id);
            Set<Permiso> permisos = rolService.obtenerPermisos(id);
            return ResponseEntity.ok(permisos);

        } catch (Exception e) {
            log.error("Error al obtener permisos del rol {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Rol no encontrado con ID: " + id));
        }
    }

    /**
     * RF-002: Verificar si un rol tiene un permiso específico.
     *
     * @param id ID del rol
     * @param nombrePermiso Nombre del permiso a verificar
     * @return Objeto con booleano indicando si tiene el permiso
     */
    @GetMapping("/{id}/tiene-permiso/{nombrePermiso}")
    @Operation(
            summary = "Verificar permiso",
            description = "Verifica si un rol tiene un permiso específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Verificación completada"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rol no encontrado"
            )
    })
    public ResponseEntity<?> tienePermiso(
            @PathVariable Long id,
            @PathVariable String nombrePermiso) {

        try {
            log.info("GET /api/roles/{}/tiene-permiso/{} - Verificar permiso", id, nombrePermiso);

            boolean tiene = rolService.tienePermiso(id, nombrePermiso);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("rolId", id);
            respuesta.put("nombrePermiso", nombrePermiso);
            respuesta.put("tienePermiso", tiene);

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            log.error("Error al verificar permiso {} del rol {}: {}", nombrePermiso, id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Rol no encontrado con ID: " + id));
        }
    }

    /**
     * Verificar si un nombre de rol ya existe.
     * Útil para validación en tiempo real en formularios.
     *
     * @param nombre Nombre a verificar
     * @return Objeto con booleano indicando si existe
     */
    @GetMapping("/existe-nombre")
    @Operation(
            summary = "Verificar nombre existente",
            description = "Verifica si un nombre de rol ya está registrado"
    )
    public ResponseEntity<Map<String, Boolean>> existeNombre(@RequestParam String nombre) {
        log.info("GET /api/roles/existe-nombre?nombre={}", nombre);
        boolean existe = rolService.existePorNombre(nombre);
        return ResponseEntity.ok(Map.of("existe", existe));
    }

    /**
     * Obtener estadísticas de roles.
     *
     * @return Estadísticas (total, activos, inactivos)
     */
    @GetMapping("/estadisticas")
    @Operation(
            summary = "Estadísticas de roles",
            description = "Obtiene estadísticas generales de roles en el sistema"
    )
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        log.info("GET /api/roles/estadisticas");

        Page<Rol> todos = rolService.listarTodos(Pageable.unpaged());
        List<Rol> activos = rolService.listarActivos();

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalRoles", todos.getTotalElements());
        estadisticas.put("rolesActivos", activos.size());
        estadisticas.put("rolesInactivos", todos.getTotalElements() - activos.size());

        return ResponseEntity.ok(estadisticas);
    }
}
