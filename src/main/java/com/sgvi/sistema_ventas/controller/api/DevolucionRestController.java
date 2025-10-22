package com.sgvi.sistema_ventas.controller.api;

import com.sgvi.sistema_ventas.model.entity.Devolucion;
import com.sgvi.sistema_ventas.model.enums.EstadoDevolucion;
import com.sgvi.sistema_ventas.service.interfaces.IDevolucionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller REST para gestión de devoluciones.
 * RF-013: Gestión de Devoluciones
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/devoluciones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Devoluciones", description = "Endpoints para gestión de devoluciones de productos")
public class DevolucionRestController {

    private final IDevolucionService devolucionService;

    /**
     * RF-013: Crear solicitud de devolución
     * POST /api/devoluciones
     */
   /* @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Crear devolución", description = "Registra una nueva solicitud de devolución")
    public ResponseEntity<Devolucion> crearDevolucion(@RequestBody Map<String, Object> data) {
        log.info("POST /api/devoluciones - Crear devolución");

        // Extraer datos del request
        Devolucion devolucion = extraerDevolucion(data);
        List<DetalleDevolucion> detalles = extraerDetalles(data);

        Devolucion devolucionCreada = devolucionService.crearDevolucion(devolucion, detalles);
        return new ResponseEntity<>(devolucionCreada, HttpStatus.CREATED);
    }*/

    /**
     * RF-013: Aprobar devolución
     * PUT /api/devoluciones/{id}/aprobar
     */
    @PutMapping("/{id}/aprobar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Aprobar devolución", description = "Aprueba una solicitud de devolución")
    public ResponseEntity<Devolucion> aprobarDevolucion(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        log.info("PUT /api/devoluciones/{}/aprobar - Aprobar devolución", id);

        Long idUsuario = body.get("idUsuario");
        Devolucion devolucionAprobada = devolucionService.aprobarDevolucion(id, idUsuario);
        return ResponseEntity.ok(devolucionAprobada);
    }

    /**
     * RF-013: Rechazar devolución
     * PUT /api/devoluciones/{id}/rechazar
     */
    @PutMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Rechazar devolución", description = "Rechaza una solicitud de devolución")
    public ResponseEntity<Devolucion> rechazarDevolucion(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        log.info("PUT /api/devoluciones/{}/rechazar - Rechazar devolución", id);

        Long idUsuario = Long.valueOf(body.get("idUsuario").toString());
        String motivo = body.get("motivo").toString();

        Devolucion devolucionRechazada = devolucionService.rechazarDevolucion(id, idUsuario, motivo);
        return ResponseEntity.ok(devolucionRechazada);
    }

    /**
     * RF-013: Completar devolución
     * PUT /api/devoluciones/{id}/completar
     */
    @PutMapping("/{id}/completar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Completar devolución", description = "Completa una devolución (actualiza stock y procesa reembolso)")
    public ResponseEntity<Devolucion> completarDevolucion(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        log.info("PUT /api/devoluciones/{}/completar - Completar devolución", id);

        Long idUsuario = body.get("idUsuario");
        Devolucion devolucionCompletada = devolucionService.completarDevolucion(id, idUsuario);
        return ResponseEntity.ok(devolucionCompletada);
    }

    /**
     * RF-013: Obtener devolución por ID
     * GET /api/devoluciones/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener devolución", description = "Obtiene una devolución por su ID")
    public ResponseEntity<Devolucion> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/devoluciones/{} - Obtener devolución", id);
        Devolucion devolucion = devolucionService.obtenerPorId(id);
        return ResponseEntity.ok(devolucion);
    }

    /**
     * RF-013: Obtener devoluciones por venta
     * GET /api/devoluciones/venta/{idVenta}
     */
    @GetMapping("/venta/{idVenta}")
    @Operation(summary = "Devoluciones por venta", description = "Lista todas las devoluciones de una venta")
    public ResponseEntity<List<Devolucion>> obtenerPorVenta(@PathVariable Long idVenta) {
        log.info("GET /api/devoluciones/venta/{} - Obtener devoluciones de venta", idVenta);
        List<Devolucion> devoluciones = devolucionService.obtenerPorVenta(idVenta);
        return ResponseEntity.ok(devoluciones);
    }

    /**
     * RF-013: Buscar devoluciones con filtros
     * GET /api/devoluciones/buscar
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar devoluciones", description = "Busca devoluciones con múltiples filtros")
    public ResponseEntity<Page<Devolucion>> buscarConFiltros(
            @RequestParam(required = false) Long idVenta,
            @RequestParam(required = false) Long idCliente,
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) EstadoDevolucion estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            Pageable pageable) {

        log.info("GET /api/devoluciones/buscar - Buscar con filtros");

        Page<Devolucion> devoluciones = devolucionService.buscarConFiltros(
                idVenta, idCliente, idUsuario, estado, fechaInicio, fechaFin, pageable
        );

        return ResponseEntity.ok(devoluciones);
    }

    /**
     * RF-013: Verificar si venta está dentro del plazo
     * GET /api/devoluciones/verificar-plazo/{idVenta}
     */
    @GetMapping("/verificar-plazo/{idVenta}")
    @Operation(summary = "Verificar plazo", description = "Verifica si una venta está dentro del plazo de devolución (30 días)")
    public ResponseEntity<Map<String, Boolean>> verificarPlazo(@PathVariable Long idVenta) {
        log.info("GET /api/devoluciones/verificar-plazo/{} - Verificar plazo", idVenta);
        boolean dentroPlazo = devolucionService.estaDentroPlazo(idVenta);
        return ResponseEntity.ok(Map.of("dentroPlazo", dentroPlazo));
    }

    /**
     * RF-013: Validar cantidad de devolución
     * GET /api/devoluciones/validar-cantidad
     */
    @GetMapping("/validar-cantidad")
    @Operation(summary = "Validar cantidad", description = "Valida si la cantidad a devolver es válida")
    public ResponseEntity<Map<String, Boolean>> validarCantidad(
            @RequestParam Long idVenta,
            @RequestParam Long idProducto,
            @RequestParam Integer cantidad) {

        log.info("GET /api/devoluciones/validar-cantidad - Venta: {}, Producto: {}, Cantidad: {}",
                idVenta, idProducto, cantidad);

        boolean esValida = devolucionService.validarCantidadDevolucion(idVenta, idProducto, cantidad);
        return ResponseEntity.ok(Map.of("cantidadValida", esValida));
    }

    /**
     * RF-014: Obtener devoluciones por período
     * GET /api/devoluciones/periodo
     */
    @GetMapping("/periodo")
    @Operation(summary = "Devoluciones por período", description = "Lista devoluciones en un rango de fechas")
    public ResponseEntity<List<Devolucion>> obtenerPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        log.info("GET /api/devoluciones/periodo - Desde: {} hasta: {}", fechaInicio, fechaFin);
        List<Devolucion> devoluciones = devolucionService.obtenerDevolucionesPorPeriodo(fechaInicio, fechaFin);
        return ResponseEntity.ok(devoluciones);
    }

    /**
     * RF-014: Análisis de motivos de devolución
     * GET /api/devoluciones/analisis-motivos
     */
    @GetMapping("/analisis-motivos")
    @Operation(summary = "Análisis de motivos", description = "Analiza los motivos de devolución más frecuentes")
    public ResponseEntity<Map<String, Long>> analizarMotivos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        log.info("GET /api/devoluciones/analisis-motivos - Análisis de motivos");
        Map<String, Long> analisis = devolucionService.analizarMotivosDevoluciones(fechaInicio, fechaFin);
        return ResponseEntity.ok(analisis);
    }
}
    /**
     * RF-014: Productos más devueltos
     * GET /api/devoluciones/productos-mas-devueltos
     */
    /*@GetMapping("/productos-mas-devueltos")
    @Operation(summary = "Productos más devueltos", description = "Lista los productos con más devoluciones")
    public ResponseEntity<List<Map<String, Object>>> obtenerProductosMasDevueltos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "10") int limite) {

        log.info("GET /api/devoluciones/productos-mas-devueltos - Top {}", limite);
        List<Map<String, Object>> productos = devolucionService.obtenerProductosMasDevueltos*/