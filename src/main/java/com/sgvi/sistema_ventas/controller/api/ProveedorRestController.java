package com.sgvi.sistema_ventas.controller.api;

import com.sgvi.sistema_ventas.model.entity.Proveedor;
import com.sgvi.sistema_ventas.service.interfaces.IProveedorService;
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

/**
 * Controller REST para gestión de proveedores.
 * RF-006: Gestión de Proveedores
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
     * RF-006: Crear proveedor
     * POST /api/proveedores
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Crear proveedor", description = "Registra un nuevo proveedor en el sistema")
    public ResponseEntity<Proveedor> crear(@Valid @RequestBody Proveedor proveedor) {
        log.info("POST /api/proveedores - Crear proveedor: {}", proveedor.getRazonSocial());
        Proveedor proveedorCreado = proveedorService.crear(proveedor);
        return new ResponseEntity<>(proveedorCreado, HttpStatus.CREATED);
    }

    /**
     * RF-006: Actualizar proveedor
     * PUT /api/proveedores/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Actualizar proveedor", description = "Actualiza datos de un proveedor existente")
    public ResponseEntity<Proveedor> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Proveedor proveedor) {
        log.info("PUT /api/proveedores/{} - Actualizar proveedor", id);
        Proveedor proveedorActualizado = proveedorService.actualizar(id, proveedor);
        return ResponseEntity.ok(proveedorActualizado);
    }

    /**
     * RF-006: Eliminar proveedor (soft delete)
     * DELETE /api/proveedores/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar proveedor", description = "Desactiva un proveedor del sistema")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/proveedores/{} - Eliminar proveedor", id);
        proveedorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * RF-006: Obtener proveedor por ID
     * GET /api/proveedores/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener proveedor", description = "Obtiene un proveedor por su ID")
    public ResponseEntity<Proveedor> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/proveedores/{} - Obtener proveedor", id);
        Proveedor proveedor = proveedorService.obtenerPorId(id);
        return ResponseEntity.ok(proveedor);
    }

    /**
     * RF-006: Buscar proveedor por RUC
     * GET /api/proveedores/ruc/{ruc}
     */
    @GetMapping("/ruc/{ruc}")
    @Operation(summary = "Buscar por RUC", description = "Obtiene un proveedor por su RUC")
    public ResponseEntity<Proveedor> buscarPorRuc(@PathVariable String ruc) {
        log.info("GET /api/proveedores/ruc/{} - Buscar por RUC", ruc);
        Proveedor proveedor = proveedorService.buscarPorRuc(ruc);
        return ResponseEntity.ok(proveedor);
    }

    /**
     * RF-006: Listar todos los proveedores
     * GET /api/proveedores?page=0&size=20
     */
    @GetMapping
    @Operation(summary = "Listar proveedores", description = "Lista todos los proveedores con paginación")
    public ResponseEntity<Page<Proveedor>> listarTodos(Pageable pageable) {
        log.info("GET /api/proveedores - Listar proveedores");
        Page<Proveedor> proveedores = proveedorService.listarTodos(pageable);
        return ResponseEntity.ok(proveedores);
    }

    /**
     * RF-006: Listar proveedores activos
     * GET /api/proveedores/activos
     */
    @GetMapping("/activos")
    @Operation(summary = "Proveedores activos", description = "Lista solo proveedores activos")
    public ResponseEntity<List<Proveedor>> listarActivos() {
        log.info("GET /api/proveedores/activos - Listar proveedores activos");
        List<Proveedor> proveedores = proveedorService.listarActivos();
        return ResponseEntity.ok(proveedores);
    }

    /**
     * RF-006: Buscar proveedores por razón social
     * GET /api/proveedores/buscar?razonSocial=Textiles
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar por razón social", description = "Busca proveedores por razón social")
    public ResponseEntity<Page<Proveedor>> buscarPorRazonSocial(
            @RequestParam String razonSocial,
            Pageable pageable) {
        log.info("GET /api/proveedores/buscar?razonSocial={}", razonSocial);
        Page<Proveedor> proveedores = proveedorService.buscarPorRazonSocial(razonSocial, pageable);
        return ResponseEntity.ok(proveedores);
    }

    /**
     * RF-006: Validar RUC
     * GET /api/proveedores/validar-ruc/{ruc}
     */
    @GetMapping("/validar-ruc/{ruc}")
    @Operation(summary = "Validar RUC", description = "Valida el formato de un RUC peruano")
    public ResponseEntity<Map<String, Object>> validarRuc(@PathVariable String ruc) {
        log.info("GET /api/proveedores/validar-ruc/{} - Validar RUC", ruc);

        boolean esValido = proveedorService.validarRuc(ruc);
        boolean existe = false;

        if (esValido) {
            existe = proveedorService.existeRuc(ruc);
        }

        return ResponseEntity.ok(Map.of(
                "ruc", ruc,
                "esValido", esValido,
                "existe", existe,
                "mensaje", esValido ?
                        (existe ? "RUC válido pero ya está registrado" : "RUC válido y disponible") :
                        "RUC inválido"
        ));
    }

    /**
     * Verificar si RUC existe
     * GET /api/proveedores/existe-ruc/{ruc}
     */
    @GetMapping("/existe-ruc/{ruc}")
    @Operation(summary = "Verificar existencia", description = "Verifica si un RUC ya está registrado")
    public ResponseEntity<Map<String, Boolean>> existeRuc(@PathVariable String ruc) {
        log.info("GET /api/proveedores/existe-ruc/{} - Verificar existencia", ruc);
        boolean existe = proveedorService.existeRuc(ruc);
        return ResponseEntity.ok(Map.of("existe", existe));
    }
}