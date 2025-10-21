package com.sgvi.sistema_ventas.controller.api;

import com.sgvi.sistema_ventas.model.entity.Permiso;
import com.sgvi.sistema_ventas.model.entity.Rol;
import com.sgvi.sistema_ventas.service.interfaces.IRolService;
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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller REST para gestión de roles y permisos.
 * RF-002: Gestión de Roles y Permisos
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
@Tag(name = "Roles", description = "Endpoints para gestión de roles y permisos (solo Admin)")
public class RolRestController {

    private final IRolService rolService;

    /**
     * RF-002: Crear rol
     * POST /api/roles
     */
    @PostMapping
    @Operation(summary = "Crear rol", description = "Crea un nuevo rol en el sistema")
    public ResponseEntity<Rol> crear(@Valid @RequestBody Rol rol) {
        log.info("POST /api/roles - Crear rol: {}", rol.getNombre());
        Rol rolCreado = rolService.crear(rol);
        return new ResponseEntity<>(rolCreado, HttpStatus.CREATED);
    }

    /**
     * RF-002: Actualizar rol
     * PUT /api/roles/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar rol", description = "Actualiza datos de un rol existente")
    public ResponseEntity<Rol> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Rol rol) {
        log.info("PUT /api/roles/{} - Actualizar rol", id);
        Rol rolActualizado = rolService.actualizar(id, rol);
        return ResponseEntity.ok(rolActualizado);
    }

    /**
     * RF-002: Eliminar rol
     * DELETE /api/roles/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar rol", description = "Elimina un rol del sistema")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/roles/{} - Eliminar rol", id);
        rolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * RF-002: Obtener rol por ID
     * GET /api/roles/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol", description = "Obtiene un rol por su ID")
    public ResponseEntity<Rol> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/roles/{} - Obtener rol", id);
        Rol rol = rolService.obtenerPorId(id);
        return ResponseEntity.ok(rol);
    }

    /**
     * RF-002: Obtener rol por nombre
     * GET /api/roles/nombre/{nombre}
     */
    @GetMapping("/nombre/{nombre}")
    @Operation(summary = "Buscar por nombre", description = "Obtiene un rol por su nombre")
    public ResponseEntity<Rol> obtenerPorNombre(@PathVariable String nombre) {
        log.info("GET /api/roles/nombre/{} - Buscar rol", nombre);
        Rol rol = rolService.obtenerPorNombre(nombre);
        return ResponseEntity.ok(rol);
    }

    /**
     * RF-002: Listar todos los roles
     * GET /api/roles?page=0&size=20
     */
    @GetMapping
    @Operation(summary = "Listar roles", description = "Lista todos los roles con paginación")
    public ResponseEntity<Page<Rol>> listarTodos(Pageable pageable) {
        log.info("GET /api/roles - Listar roles");
        Page<Rol> roles = rolService.listarTodos(pageable);
        return ResponseEntity.ok(roles);
    }

    /**
     * RF-002: Listar roles activos
     * GET /api/roles/activos
     */
    @GetMapping("/activos")
    @Operation(summary = "Roles activos", description = "Lista solo roles activos")
    public ResponseEntity<List<Rol>> listarActivos() {
        log.info("GET /api/roles/activos - Listar roles activos");
        List<Rol> roles = rolService.listarActivos();
        return ResponseEntity.ok(roles);
    }

    /**
     * RF-002: Asignar permisos a un rol
     * POST /api/roles/{id}/permisos
     */
    @PostMapping("/{id}/permisos")
    @Operation(summary = "Asignar permisos", description = "Asigna una lista de permisos a un rol")
    public ResponseEntity<Map<String, String>> asignarPermisos(
            @PathVariable Long id,
            @RequestBody List<Long> idsPermisos) {
        log.info("POST /api/roles/{}/permisos - Asignar {} permisos", id, idsPermisos.size());
        rolService.asignarPermisos(id, idsPermisos);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Permisos asignados exitosamente",
                "rolId", id.toString(),
                "cantidadPermisos", String.valueOf(idsPermisos.size())
        ));
    }

    /**
     * RF-002: Remover permiso de un rol
     * DELETE /api/roles/{id}/permisos/{idPermiso}
     */
    @DeleteMapping("/{id}/permisos/{idPermiso}")
    @Operation(summary = "Remover permiso", description = "Elimina un permiso específico de un rol")
    public ResponseEntity<Void> removerPermiso(
            @PathVariable Long id,
            @PathVariable Long idPermiso) {
        log.info("DELETE /api/roles/{}/permisos/{} - Remover permiso", id, idPermiso);
        rolService.removerPermiso(id, idPermiso);
        return ResponseEntity.noContent().build();
    }

    /**
     * RF-002: Obtener permisos de un rol
     * GET /api/roles/{id}/permisos
     */
    @GetMapping("/{id}/permisos")
    @Operation(summary = "Obtener permisos", description = "Lista todos los permisos de un rol")
    public ResponseEntity<Set<Permiso>> obtenerPermisos(@PathVariable Long id) {
        log.info("GET /api/roles/{}/permisos - Obtener permisos del rol", id);
        Set<Permiso> permisos = rolService.obtenerPermisos(id);
        return ResponseEntity.ok(permisos);
    }

    /**
     * RF-002: Verificar si rol tiene permiso específico
     * GET /api/roles/{id}/tiene-permiso/{nombrePermiso}
     */
    @GetMapping("/{id}/tiene-permiso/{nombrePermiso}")
    @Operation(summary = "Verificar permiso", description = "Verifica si un rol tiene un permiso específico")
    public ResponseEntity<Map<String, Boolean>> tienePermiso(
            @PathVariable Long id,
            @PathVariable String nombrePermiso) {
        log.info("GET /api/roles/{}/tiene-permiso/{} - Verificar permiso", id, nombrePermiso);
        boolean tiene = rolService.tienePermiso(id, nombrePermiso);
        return ResponseEntity.ok(Map.of("tienePermiso", tiene));
    }
}
