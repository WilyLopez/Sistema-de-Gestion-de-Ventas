package com.sgvi.sistema_ventas.controller.api;

import com.sgvi.sistema_ventas.model.entity.DetalleVenta;
import com.sgvi.sistema_ventas.model.entity.Venta;
import com.sgvi.sistema_ventas.model.enums.EstadoVenta;
import com.sgvi.sistema_ventas.service.interfaces.IVentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller REST para gestión de ventas.
 * RF-007, RF-008, RF-009: Gestión de Ventas
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ventas", description = "Endpoints para gestión de ventas")
public class VentaRestController {

    private final IVentaService ventaService;

    /**
     * RF-007: Registrar venta
     * POST /api/ventas
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Registrar venta", description = "Registra una nueva venta con sus detalles")
    public ResponseEntity<Venta> registrarVenta(@RequestBody Map<String, Object> ventaData) {
        log.info("POST /api/ventas - Registrar venta");

        // Extraer venta y detalles del request
        Venta venta = extraerVenta(ventaData);
        List<DetalleVenta> detalles = extraerDetalles(ventaData);

        Venta ventaCreada = ventaService.registrarVenta(venta, detalles);
        return new ResponseEntity<>(ventaCreada, HttpStatus.CREATED);
    }

    /**
     * RF-008: Obtener venta por ID
     * GET /api/ventas/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener venta", description = "Obtiene una venta por su ID")
    public ResponseEntity<Venta> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/ventas/{}", id);
        Venta venta = ventaService.obtenerPorId(id);
        return ResponseEntity.ok(venta);
    }

    /**
     * RF-008: Listar ventas con paginación
     * GET /api/ventas?page=0&size=20
     */
    @GetMapping
    @Operation(summary = "Listar ventas", description = "Lista todas las ventas con paginación")
    public ResponseEntity<Page<Venta>> listarTodas(Pageable pageable) {
        log.info("GET /api/ventas - Listar ventas");
        Page<Venta> ventas = ventaService.listarTodas(pageable);
        return ResponseEntity.ok(ventas);
    }

    /**
     * RF-008: Buscar ventas con filtros
     * GET /api/ventas/buscar?estado=PAGADO&fechaInicio=2024-01-01
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar ventas", description = "Busca ventas con múltiples filtros")
    public ResponseEntity<Page<Venta>> buscarConFiltros(
            @RequestParam(required = false) String codigoVenta,
            @RequestParam(required = false) Long idCliente,
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) EstadoVenta estado,
            @RequestParam(required = false) Long idMetodoPago,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            Pageable pageable) {

        log.info("GET /api/ventas/buscar - Filtros aplicados");

        Page<Venta> ventas = ventaService.buscarConFiltros(
                codigoVenta, idCliente, idUsuario, estado, idMetodoPago,
                fechaInicio, fechaFin, pageable
        );

        return ResponseEntity.ok(ventas);
    }

    /**
     * RF-009: Anular venta
     * PUT /api/ventas/{id}/anular
     */
    @PutMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Anular venta", description = "Anula una venta (solo si tiene menos de 24h)")
    public ResponseEntity<Void> anularVenta(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        log.info("PUT /api/ventas/{}/anular", id);
        String motivo = body.get("motivo");
        ventaService.anularVenta(id, motivo);
        return ResponseEntity.noContent().build();
    }

    /**
     * RF-009: Verificar si venta puede anularse
     * GET /api/ventas/{id}/puede-anularse
     */
    @GetMapping("/{id}/puede-anularse")
    @Operation(summary = "Verificar anulación", description = "Verifica si una venta puede ser anulada")
    public ResponseEntity<Map<String, Boolean>> puedeAnularse(@PathVariable Long id) {
        log.info("GET /api/ventas/{}/puede-anularse", id);
        boolean puede = ventaService.puedeAnularse(id);
        return ResponseEntity.ok(Map.of("puedeAnularse", puede));
    }

    /**
     * RF-008: Obtener venta por código
     * GET /api/ventas/codigo/{codigo}
     */
    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Buscar por código", description = "Obtiene una venta por su código")
    public ResponseEntity<Venta> obtenerPorCodigo(@PathVariable String codigo) {
        log.info("GET /api/ventas/codigo/{}", codigo);
        Venta venta = ventaService.obtenerPorCodigo(codigo);
        return ResponseEntity.ok(venta);
    }

    // Métodos auxiliares privados
    private Venta extraerVenta(Map<String, Object> data) {
        // TODO: Implementar extracción y conversión de datos
        // Por ahora retornamos un objeto básico
        return new Venta();
    }

    private List<DetalleVenta> extraerDetalles(Map<String, Object> data) {
        // TODO: Implementar extracción de detalles
        return List.of();
    }
}
