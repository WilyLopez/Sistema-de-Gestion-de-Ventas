package com.sgvi.sistema_ventas.controller;

import com.sgvi.sistema_ventas.model.dto.rol.RolMapper;
import com.sgvi.sistema_ventas.model.dto.auth.MessageResponse;
import com.sgvi.sistema_ventas.model.dto.rol.PermisoDTO;
import com.sgvi.sistema_ventas.model.dto.rol.AsignarPermisosRequest;
import com.sgvi.sistema_ventas.model.dto.rol.RolConPermisosDTO;
import com.sgvi.sistema_ventas.model.dto.rol.RolDTO;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMINISTRADOR')")
@Tag(name = "Roles", description = "Endpoints para gestión de roles y permisos (solo Administrador)")
public class RolRestController {

    private final IRolService rolService;
    private final RolMapper rolMapper;  // ⚠️ AGREGAR ESTA INYECCIÓN

    @PostMapping
    @Operation(summary = "Crear rol")
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
            RolDTO rolDTO = rolMapper.toDTO(rolCreado);  // ⚠️ CONVERTIR A DTO

            log.info("Rol creado exitosamente: {} - ID: {}", rolCreado.getNombre(), rolCreado.getIdRol());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(rolDTO);  // ⚠️ DEVOLVER DTO

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

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar rol")
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
            RolDTO rolDTO = rolMapper.toDTO(rolActualizado);  // ⚠️ CONVERTIR A DTO

            log.info("Rol actualizado exitosamente: {}", id);

            return ResponseEntity.ok(rolDTO);  // ⚠️ DEVOLVER DTO

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

    // ⚠️ MÉTODO SIN CAMBIOS
    @DeleteMapping("/{id}")
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

    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol por ID")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            log.info("GET /api/roles/{} - Obtener rol", id);
            Rol rol = rolService.obtenerPorIdConPermisos(id);  // ⚠️ USAR MÉTODO CON PERMISOS
            RolConPermisosDTO rolDTO = rolMapper.toDTOConPermisos(rol);  // ⚠️ CONVERTIR A DTO CON PERMISOS
            return ResponseEntity.ok(rolDTO);

        } catch (Exception e) {
            log.error("Error al obtener rol {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Rol no encontrado con ID: " + id));
        }
    }

    @GetMapping("/nombre/{nombre}")
    @Operation(summary = "Buscar por nombre")
    public ResponseEntity<?> obtenerPorNombre(@PathVariable String nombre) {
        try {
            log.info("GET /api/roles/nombre/{} - Buscar rol", nombre);
            Rol rol = rolService.obtenerPorNombre(nombre);
            RolDTO rolDTO = rolMapper.toDTO(rol);  // ⚠️ CONVERTIR A DTO
            return ResponseEntity.ok(rolDTO);

        } catch (Exception e) {
            log.error("Error al buscar rol por nombre {}: {}", nombre, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Rol no encontrado: " + nombre));
        }
    }

    @GetMapping
    @Operation(summary = "Listar roles")
    public ResponseEntity<Page<RolDTO>> listarTodos(Pageable pageable) {  // ⚠️ CAMBIAR A RolDTO
        log.info("GET /api/roles - Listar roles (página: {})", pageable.getPageNumber());
        Page<Rol> roles = rolService.listarTodos(pageable);
        Page<RolDTO> rolesDTO = roles.map(rolMapper::toDTO);  // ⚠️ CONVERTIR A DTO
        return ResponseEntity.ok(rolesDTO);
    }

    @GetMapping("/activos")
    @Operation(summary = "Roles activos")
    public ResponseEntity<List<RolDTO>> listarActivos() {  // ⚠️ CAMBIAR A RolDTO
        log.info("GET /api/roles/activos - Listar roles activos");
        List<Rol> roles = rolService.listarActivos();
        List<RolDTO> rolesDTO = roles.stream()  // ⚠️ CONVERTIR A DTO
                .map(rolMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rolesDTO);
    }

    // ⚠️ RESTO DE MÉTODOS SIN CAMBIOS (asignarPermisos, removerPermiso, etc.)
    @PostMapping("/{id}/permisos")
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

    @DeleteMapping("/{id}/permisos/{idPermiso}")
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

    @GetMapping("/{id}/permisos")
    public ResponseEntity<?> obtenerPermisos(@PathVariable Long id) {
        try {
            log.info("GET /api/roles/{}/permisos - Obtener permisos del rol", id);
            Set<Permiso> permisos = rolService.obtenerPermisos(id);

            // Convertir a DTO para evitar LazyInitializationException
            Set<PermisoDTO> permisosDTO = permisos.stream()
                    .map(p -> PermisoDTO.builder()
                            .idPermiso(p.getIdPermiso())
                            .nombre(p.getNombre())
                            .descripcion(p.getDescripcion())
                            .categoria(p.getModulo())
                            .build())
                    .collect(Collectors.toSet());

            return ResponseEntity.ok(permisosDTO);

        } catch (Exception e) {
            log.error("Error al obtener permisos del rol {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Rol no encontrado con ID: " + id));
        }
    }

    @GetMapping("/{id}/tiene-permiso/{nombrePermiso}")
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

    @GetMapping("/existe-nombre")
    public ResponseEntity<Map<String, Boolean>> existeNombre(@RequestParam String nombre) {
        log.info("GET /api/roles/existe-nombre?nombre={}", nombre);
        boolean existe = rolService.existePorNombre(nombre);
        return ResponseEntity.ok(Map.of("existe", existe));
    }

    @GetMapping("/estadisticas")
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