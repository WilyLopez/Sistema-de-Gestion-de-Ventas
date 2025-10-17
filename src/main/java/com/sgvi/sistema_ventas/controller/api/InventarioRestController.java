package com.sgvi.sistema_ventas.controller.api;

import com.sgvi.sistema_ventas.model.entity.Inventario;
import com.sgvi.sistema_ventas.model.enums.TipoMovimiento;
import com.sgvi.sistema_ventas.service.interfaces.IInventarioService;
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
 * Controller REST para gestión de inventario.
 * RF-012: Gestión de Inventario
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventario", description = "Endpoints para gestión de inventario")
public class InventarioRestController {

    private final IInventarioService inventarioService;

    /**
     * RF-012: Registrar entrada de producto
     * POST /api/inventario/entrada
     */
    @PostMapping("/entrada")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(summary = "Registrar entrada", description = "Registra entrada de productos al inventario")
    public ResponseEntity<Inventario> registrarEntrada(@RequestBody Map<String, Object> data) {
        log.info("POST /api/inventario/entrada");

        Long idProducto = Long.valueOf(data.get("idProducto").toString());
        Integer cantidad = Integer.valueOf(data.get("cantidad").toString());
        Long idUsuario = Long.valueOf(data.get("idUsuario").toString());
        String observacion = data.get("observacion") != null ? data.get("observacion").toString() : "";

        Inventario movimiento = inventarioService.registrarEntrada(idProducto, cantidad, idUsuario, observacion);
        return new ResponseEntity<>(movimiento, HttpStatus.CREATED);
    }

    /**
     * RF-012: Registrar ajuste de inventario
     * POST /api/inventario/ajuste
     */
    @PostMapping("/ajuste")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Registrar ajuste", description = "Ajusta el stock de un producto manualmente")
    public ResponseEntity<Inventario> registrarAjuste(@RequestBody Map<String, Object> data) {
        log.info("POST /api/inventario/ajuste");

        Long idProducto = Long.valueOf(data.get("idProducto").toString());
        Integer nuevoStock = Integer.valueOf(data.get("nuevoStock").toString());
        Long idUsuario = Long.valueOf(data.get("idUsuario").toString());
        String observacion = data.get("observacion").toString();

        Inventario movimiento = inventarioService.registrarAjuste(idProducto, nuevoStock, idUsuario, observacion);
        return new ResponseEntity<>(movimiento, HttpStatus.CREATED);
    }

    /**
     * RF-012: Obtener movimientos por producto
     * GET /api/inventario/producto/{idProducto}
     */
    @GetMapping("/producto/{idProducto}")
    @Operation(summary = "Movimientos por producto", description = "Lista movimientos de un producto específico")
    public ResponseEntity<Page<Inventario>> obtenerMovimientosPorProducto(
            @PathVariable Long idProducto,
            Pageable pageable) {
        log.info("GET /api/inventario/producto/{}", idProducto);
        Page<Inventario> movimientos = inventarioService.obtenerMovimientosPorProducto(idProducto, pageable);
        return ResponseEntity.ok(movimientos);
    }

    /**
     * RF-012: Obtener trazabilidad completa de un producto
     * GET /api/inventario/trazabilidad/{idProducto}
     */
    @GetMapping("/trazabilidad/{idProducto}")
    @Operation(summary = "Trazabilidad de producto", description = "Obtiene historial completo de movimientos")
    public ResponseEntity<List<Inventario>> obtenerTrazabilidad(@PathVariable Long idProducto) {
        log.info("GET /api/inventario/trazabilidad/{}", idProducto);
        List<Inventario> movimientos = inventarioService.obtenerTrazabilidad(idProducto);
        return ResponseEntity.ok(movimientos);
    }

    /**
     * RF-012: Buscar movimientos con filtros
     * GET /api/inventario/buscar
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar movimientos", description = "Busca movimientos con múltiples filtros")
    public ResponseEntity<Page<Inventario>> buscarConFiltros(
            @RequestParam(required = false) Long idProducto,
            @RequestParam(required = false) TipoMovimiento tipoMovimiento,
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            Pageable pageable) {

        log.info("GET /api/inventario/buscar");

        Page<Inventario> movimientos = inventarioService.buscarMovimientosConFiltros(
                idProducto, tipoMovimiento, idUsuario, fechaInicio, fechaFin, pageable
        );

        return ResponseEntity.ok(movimientos);
    }
}

