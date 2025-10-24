package com.sgvi.sistema_ventas.controller;

import com.sgvi.sistema_ventas.model.dto.auth.MessageResponse;
import com.sgvi.sistema_ventas.model.entity.Proveedor;
import com.sgvi.sistema_ventas.service.interfaces.IProveedorService;
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

/**
 * Controlador REST para gestión de proveedores.
 * Implementa requisito RF-006 del SRS: Gestión de Proveedores.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Proveedores", description = "Endpoints para gestión de proveedores")
public class ProveedorRestController {

    private final IProveedorService proveedorService;

    /**
     * RF-006: Crear nuevo proveedor.
     * Valida RUC único y formato válido.
     *
     * @param proveedor Datos del proveedor
     * @return Proveedor creado
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Crear proveedor",
            description = "Registra un nuevo proveedor. Valida RUC peruano y unicidad"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Proveedor creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Proveedor.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o RUC duplicado/inválido",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos"
            )
    })
    public ResponseEntity<?> crear(@Valid @RequestBody Proveedor proveedor) {
        try {
            log.info("POST /api/proveedores - Crear proveedor: {}", proveedor.getRazonSocial());

            if (proveedor.getRuc() != null && !proveedor.getRuc().trim().isEmpty()) {
                if (!proveedorService.validarRuc(proveedor.getRuc())) {
                    log.warn("Intento de crear proveedor con RUC inválido: {}", proveedor.getRuc());
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("El RUC no es válido: " + proveedor.getRuc()));
                }

                if (proveedorService.existeRuc(proveedor.getRuc())) {
                    log.warn("Intento de crear proveedor con RUC duplicado: {}", proveedor.getRuc());
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("El RUC ya está registrado: " + proveedor.getRuc()));
                }
            }

            Proveedor proveedorCreado = proveedorService.crear(proveedor);

            log.info("Proveedor creado exitosamente: {} - ID: {}",
                    proveedorCreado.getRazonSocial(),
                    proveedorCreado.getIdProveedor());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(proveedorCreado);

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al crear proveedor: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al crear proveedor: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al crear proveedor. Intente nuevamente"));
        }
    }

    /**
     * RF-006: Actualizar proveedor existente.
     * Valida RUC si se modifica.
     *
     * @param id ID del proveedor
     * @param proveedor Datos actualizados
     * @return Proveedor actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Actualizar proveedor",
            description = "Actualiza datos de un proveedor existente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Proveedor actualizado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o RUC duplicado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Proveedor no encontrado"
            )
    })
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Proveedor proveedor) {

        try {
            log.info("PUT /api/proveedores/{} - Actualizar proveedor", id);

            Proveedor proveedorExistente = proveedorService.obtenerPorId(id);

            if (proveedor.getRuc() != null &&
                    !proveedor.getRuc().equals(proveedorExistente.getRuc())) {

                if (!proveedorService.validarRuc(proveedor.getRuc())) {
                    log.warn("Intento de actualizar con RUC inválido: {}", proveedor.getRuc());
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("El RUC no es válido: " + proveedor.getRuc()));
                }

                if (proveedorService.existeRuc(proveedor.getRuc())) {
                    log.warn("Intento de actualizar con RUC duplicado: {}", proveedor.getRuc());
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("El RUC ya está registrado: " + proveedor.getRuc()));
                }
            }

            Proveedor proveedorActualizado = proveedorService.actualizar(id, proveedor);

            log.info("Proveedor actualizado exitosamente: {}", id);

            return ResponseEntity.ok(proveedorActualizado);

        } catch (IllegalArgumentException e) {
            log.warn("Datos inválidos al actualizar proveedor {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al actualizar proveedor {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Proveedor no encontrado con ID: " + id));
        }
    }

    /**
     * RF-006: Eliminar proveedor (soft delete).
     * Marca el proveedor como inactivo.
     *
     * @param id ID del proveedor
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Eliminar proveedor",
            description = "Desactiva un proveedor del sistema (soft delete). Solo administrador"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Proveedor eliminado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos (solo administrador)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Proveedor no encontrado"
            )
    })
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            log.info("DELETE /api/proveedores/{} - Eliminar proveedor", id);

            proveedorService.eliminar(id);

            log.info("Proveedor eliminado exitosamente: {}", id);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error al eliminar proveedor {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Proveedor no encontrado con ID: " + id));
        }
    }

    /**
     * RF-006: Obtener proveedor por ID.
     *
     * @param id ID del proveedor
     * @return Proveedor encontrado
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener proveedor por ID",
            description = "Obtiene los datos completos de un proveedor"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Proveedor encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Proveedor no encontrado"
            )
    })
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            log.info("GET /api/proveedores/{} - Obtener proveedor", id);
            Proveedor proveedor = proveedorService.obtenerPorId(id);
            return ResponseEntity.ok(proveedor);

        } catch (Exception e) {
            log.error("Error al obtener proveedor {}: {}", id, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Proveedor no encontrado con ID: " + id));
        }
    }

    /**
     * RF-006: Buscar proveedor por RUC.
     *
     * @param ruc RUC del proveedor
     * @return Proveedor encontrado
     */
    @GetMapping("/ruc/{ruc}")
    @Operation(
            summary = "Buscar por RUC",
            description = "Obtiene un proveedor por su número de RUC"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Proveedor encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Proveedor no encontrado con ese RUC"
            )
    })
    public ResponseEntity<?> buscarPorRuc(@PathVariable String ruc) {
        try {
            log.info("GET /api/proveedores/ruc/{} - Buscar por RUC", ruc);
            Proveedor proveedor = proveedorService.buscarPorRuc(ruc);
            return ResponseEntity.ok(proveedor);

        } catch (Exception e) {
            log.error("Error al buscar proveedor por RUC {}: {}", ruc, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Proveedor no encontrado con RUC: " + ruc));
        }
    }

    /**
     * RF-006: Listar todos los proveedores con paginación.
     *
     * @param pageable Parámetros de paginación
     * @return Página de proveedores
     */
    @GetMapping
    @Operation(
            summary = "Listar proveedores",
            description = "Lista todos los proveedores con paginación. Ejemplo: ?page=0&size=20&sort=razonSocial,asc"
    )
    public ResponseEntity<Page<Proveedor>> listarTodos(Pageable pageable) {
        log.info("GET /api/proveedores - Listar proveedores (página: {})", pageable.getPageNumber());
        Page<Proveedor> proveedores = proveedorService.listarTodos(pageable);
        return ResponseEntity.ok(proveedores);
    }

    /**
     * RF-006: Listar solo proveedores activos.
     * Útil para selects/dropdowns en formularios.
     *
     * @return Lista de proveedores activos
     */
    @GetMapping("/activos")
    @Operation(
            summary = "Proveedores activos",
            description = "Lista solo proveedores activos ordenados por razón social. Útil para formularios"
    )
    public ResponseEntity<List<Proveedor>> listarActivos() {
        log.info("GET /api/proveedores/activos - Listar proveedores activos");
        List<Proveedor> proveedores = proveedorService.listarActivos();
        return ResponseEntity.ok(proveedores);
    }

    /**
     * RF-006: Buscar proveedores por razón social.
     *
     * @param razonSocial Texto a buscar
     * @param pageable Parámetros de paginación
     * @return Página de proveedores que coinciden
     */
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar por razón social",
            description = "Busca proveedores por razón social (búsqueda case-insensitive)"
    )
    public ResponseEntity<Page<Proveedor>> buscarPorRazonSocial(
            @RequestParam String razonSocial,
            Pageable pageable) {

        log.info("GET /api/proveedores/buscar?razonSocial={}", razonSocial);
        Page<Proveedor> proveedores = proveedorService.buscarPorRazonSocial(razonSocial, pageable);
        return ResponseEntity.ok(proveedores);
    }

    /**
     * RF-006: Validar RUC peruano.
     * Verifica formato, dígito verificador y si ya está registrado.
     *
     * @param ruc RUC a validar
     * @return Objeto con resultado de validación
     */
    @GetMapping("/validar-ruc/{ruc}")
    @Operation(
            summary = "Validar RUC",
            description = "Valida el formato de un RUC peruano y verifica si ya está registrado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Validación completada"
            )
    })
    public ResponseEntity<Map<String, Object>> validarRuc(@PathVariable String ruc) {
        try {
            log.info("GET /api/proveedores/validar-ruc/{} - Validar RUC", ruc);

            boolean esValido = proveedorService.validarRuc(ruc);
            boolean existe = false;

            if (esValido) {
                existe = proveedorService.existeRuc(ruc);
            }

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("ruc", ruc);
            respuesta.put("esValido", esValido);
            respuesta.put("existe", existe);
            respuesta.put("puedeRegistrar", esValido && !existe);

            if (!esValido) {
                respuesta.put("mensaje", "RUC inválido. Verifique el formato");
            } else if (existe) {
                respuesta.put("mensaje", "RUC válido pero ya está registrado");
            } else {
                respuesta.put("mensaje", "RUC válido y disponible para registro");
            }

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            log.error("Error al validar RUC {}: {}", ruc, e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "ruc", ruc,
                    "esValido", false,
                    "existe", false,
                    "puedeRegistrar", false,
                    "mensaje", "Error al validar RUC"
            ));
        }
    }

    /**
     * Verificar si un RUC ya está registrado.
     * Útil para validación en tiempo real en formularios.
     *
     * @param ruc RUC a verificar
     * @return Objeto con booleano indicando si existe
     */
    @GetMapping("/existe-ruc/{ruc}")
    @Operation(
            summary = "Verificar existencia de RUC",
            description = "Verifica si un RUC ya está registrado en el sistema"
    )
    public ResponseEntity<Map<String, Boolean>> existeRuc(@PathVariable String ruc) {
        log.info("GET /api/proveedores/existe-ruc/{} - Verificar existencia", ruc);
        boolean existe = proveedorService.existeRuc(ruc);
        return ResponseEntity.ok(Map.of("existe", existe));
    }

    /**
     * Obtener estadísticas de proveedores.
     * Solo administrador.
     *
     * @return Estadísticas (total, activos, inactivos)
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Estadísticas de proveedores",
            description = "Obtiene estadísticas generales de proveedores"
    )
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        log.info("GET /api/proveedores/estadisticas");

        Page<Proveedor> todos = proveedorService.listarTodos(Pageable.unpaged());
        List<Proveedor> activos = proveedorService.listarActivos();

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalProveedores", todos.getTotalElements());
        estadisticas.put("proveedoresActivos", activos.size());
        estadisticas.put("proveedoresInactivos", todos.getTotalElements() - activos.size());

        return ResponseEntity.ok(estadisticas);
    }
}
