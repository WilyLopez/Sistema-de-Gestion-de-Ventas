package com.sgvi.sistema_ventas.controller;

import com.sgvi.sistema_ventas.model.dto.auth.MessageResponse;
import com.sgvi.sistema_ventas.model.dto.inventario.RegistrarAjusteDTO;
import com.sgvi.sistema_ventas.model.dto.inventario.RegistrarDevolucionDTO;
import com.sgvi.sistema_ventas.model.dto.inventario.RegistrarEntradaDTO;
import com.sgvi.sistema_ventas.model.entity.Inventario;
import com.sgvi.sistema_ventas.model.enums.TipoMovimiento;
import com.sgvi.sistema_ventas.service.interfaces.IInventarioService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de inventario.
 * Implementa requisito RF-012 del SRS: Gestión de Inventario.
 *
 * Funcionalidades:
 * - Registro de movimientos (entradas, salidas, ajustes, devoluciones)
 * - Trazabilidad completa de productos
 * - Búsqueda avanzada de movimientos
 * - Consultas para reportes
 *
 * @author Wilian Lopez
 * @version 1.1
 * @since 2024
 */
@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventario", description = "Endpoints para gestión de movimientos de inventario y trazabilidad")
public class InventarioRestController {

    private final IInventarioService inventarioService;

    /**
     * RF-012: Registrar entrada de productos al inventario.
     * Incrementa el stock disponible (compras, reposiciones).
     *
     * @param entradaDTO Datos de la entrada
     * @return Movimiento de inventario registrado
     */
    @PostMapping("/entrada")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Registrar entrada",
            description = "Registra entrada de productos al inventario (compras, reabastecimiento). Incrementa el stock"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Entrada registrada exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto o usuario no encontrado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos"
            )
    })
    public ResponseEntity<?> registrarEntrada(@Valid @RequestBody RegistrarEntradaDTO entradaDTO) {
        try {
            log.info("POST /api/inventario/entrada - Producto: {}, Cantidad: {}",
                    entradaDTO.getIdProducto(), entradaDTO.getCantidad());

            Inventario movimiento = inventarioService.registrarEntrada(
                    entradaDTO.getIdProducto(),
                    entradaDTO.getCantidad(),
                    entradaDTO.getIdUsuario(),
                    entradaDTO.getObservacion()
            );

            log.info("Entrada registrada exitosamente. Movimiento ID: {}", movimiento.getIdMovimiento());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(movimiento);

        } catch (IllegalArgumentException e) {
            log.warn("Producto o usuario no encontrado: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al registrar entrada: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al registrar entrada al inventario"));
        }
    }

    /**
     * RF-012: Registrar ajuste manual de inventario.
     * Establece un nuevo stock directamente (correcciones, auditorías).
     * Solo administrador.
     *
     * @param ajusteDTO Datos del ajuste
     * @return Movimiento de inventario registrado
     */
    @PostMapping("/ajuste")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Registrar ajuste",
            description = "Ajusta el stock de un producto manualmente. Establece un valor exacto. Solo administrador. Requiere observación detallada"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Ajuste registrado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o stock negativo"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos (solo administrador)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto o usuario no encontrado"
            )
    })
    public ResponseEntity<?> registrarAjuste(@Valid @RequestBody RegistrarAjusteDTO ajusteDTO) {
        try {
            log.info("POST /api/inventario/ajuste - Producto: {}, Nuevo Stock: {}",
                    ajusteDTO.getIdProducto(), ajusteDTO.getNuevoStock());

            Inventario movimiento = inventarioService.registrarAjuste(
                    ajusteDTO.getIdProducto(),
                    ajusteDTO.getNuevoStock(),
                    ajusteDTO.getIdUsuario(),
                    ajusteDTO.getObservacion()
            );

            log.info("Ajuste registrado exitosamente. Movimiento ID: {}", movimiento.getIdMovimiento());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(movimiento);

        } catch (IllegalArgumentException e) {
            log.warn("Error en datos del ajuste: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al registrar ajuste: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al registrar ajuste de inventario"));
        }
    }

    /**
     * RF-012: Registrar devolución de productos.
     * Incrementa el stock por devolución de clientes.
     *
     * @param devolucionDTO Datos de la devolución
     * @return Movimiento de inventario registrado
     */
    @PostMapping("/devolucion")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
    @Operation(
            summary = "Registrar devolución",
            description = "Registra devolución de productos al inventario. Incrementa el stock"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Devolución registrada exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto o usuario no encontrado"
            )
    })
    public ResponseEntity<?> registrarDevolucion(@Valid @RequestBody RegistrarDevolucionDTO devolucionDTO) {
        try {
            log.info("POST /api/inventario/devolucion - Producto: {}, Cantidad: {}",
                    devolucionDTO.getIdProducto(), devolucionDTO.getCantidad());

            Inventario movimiento = inventarioService.registrarDevolucion(
                    devolucionDTO.getIdProducto(),
                    devolucionDTO.getCantidad(),
                    devolucionDTO.getIdUsuario(),
                    devolucionDTO.getObservacion()
            );

            log.info("Devolución registrada exitosamente. Movimiento ID: {}", movimiento.getIdMovimiento());

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(movimiento);

        } catch (IllegalArgumentException e) {
            log.warn("Error en datos de devolución: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al registrar devolución: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al registrar devolución"));
        }
    }

    /**
     * RF-012: Obtener movimientos de un producto específico con paginación.
     *
     * @param idProducto ID del producto
     * @param pageable Parámetros de paginación
     * @return Página de movimientos del producto
     */
    @GetMapping("/producto/{idProducto}")
    @Operation(
            summary = "Movimientos por producto",
            description = "Lista todos los movimientos de inventario de un producto específico con paginación"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Movimientos obtenidos exitosamente"
            )
    })
    public ResponseEntity<?> obtenerMovimientosPorProducto(
            @PathVariable Long idProducto,
            Pageable pageable) {

        try {
            log.info("GET /api/inventario/producto/{} - Página: {}", idProducto, pageable.getPageNumber());

            Page<Inventario> movimientos = inventarioService.obtenerMovimientosPorProducto(idProducto, pageable);

            log.debug("Movimientos encontrados: {}", movimientos.getTotalElements());

            return ResponseEntity.ok(movimientos);

        } catch (Exception e) {
            log.error("Error al obtener movimientos del producto {}: {}", idProducto, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener movimientos del producto"));
        }
    }

    /**
     * RF-012: Obtener trazabilidad completa de un producto.
     * Historial ordenado cronológicamente de todos los movimientos.
     *
     * @param idProducto ID del producto
     * @return Lista completa de movimientos
     */
    @GetMapping("/trazabilidad/{idProducto}")
    @Operation(
            summary = "Trazabilidad de producto",
            description = "Obtiene historial completo de movimientos de un producto ordenado cronológicamente. Útil para auditorías"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trazabilidad obtenida exitosamente"
            )
    })
    public ResponseEntity<?> obtenerTrazabilidad(@PathVariable Long idProducto) {
        try {
            log.info("GET /api/inventario/trazabilidad/{} - Obtener historial completo", idProducto);

            List<Inventario> movimientos = inventarioService.obtenerTrazabilidad(idProducto);

            log.info("Trazabilidad obtenida: {} movimientos históricos", movimientos.size());

            return ResponseEntity.ok(movimientos);

        } catch (Exception e) {
            log.error("Error al obtener trazabilidad del producto {}: {}", idProducto, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener trazabilidad"));
        }
    }

    /**
     * RF-012: Buscar movimientos con múltiples filtros opcionales.
     *
     * @param idProducto ID del producto (opcional)
     * @param tipoMovimiento Tipo de movimiento (opcional)
     * @param idUsuario ID del usuario (opcional)
     * @param fechaInicio Fecha inicial (opcional)
     * @param fechaFin Fecha final (opcional)
     * @param pageable Parámetros de paginación
     * @return Página de movimientos filtrados
     */
    @GetMapping("/buscar")
    @Operation(
            summary = "Buscar movimientos con filtros",
            description = "Busca movimientos de inventario aplicando múltiples filtros opcionales. Todos los parámetros son opcionales y combinables"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Búsqueda completada exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetros de búsqueda inválidos"
            )
    })
    public ResponseEntity<?> buscarConFiltros(
            @RequestParam(required = false) Long idProducto,
            @RequestParam(required = false) TipoMovimiento tipoMovimiento,
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            Pageable pageable) {

        try {
            log.info("GET /api/inventario/buscar - Filtros: producto={}, tipo={}, usuario={}",
                    idProducto, tipoMovimiento, idUsuario);

            // Validar rango de fechas
            if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("La fecha de inicio no puede ser posterior a la fecha fin"));
            }

            Page<Inventario> movimientos = inventarioService.buscarMovimientosConFiltros(
                    idProducto, tipoMovimiento, idUsuario, fechaInicio, fechaFin, pageable
            );

            log.debug("Movimientos encontrados: {}", movimientos.getTotalElements());

            return ResponseEntity.ok(movimientos);

        } catch (Exception e) {
            log.error("Error al buscar movimientos: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al buscar movimientos"));
        }
    }

    /**
     * Listar todos los movimientos con paginación.
     *
     * @param pageable Parámetros de paginación
     * @return Página de movimientos
     */
    @GetMapping
    @Operation(
            summary = "Listar todos los movimientos",
            description = "Lista todos los movimientos de inventario con paginación"
    )
    public ResponseEntity<?> listarTodos(Pageable pageable) {
        try {
            log.info("GET /api/inventario - Listar todos - Página: {}", pageable.getPageNumber());

            Page<Inventario> movimientos = inventarioService.buscarMovimientosConFiltros(
                    null, null, null, null, null, pageable
            );

            return ResponseEntity.ok(movimientos);

        } catch (Exception e) {
            log.error("Error al listar movimientos: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al listar movimientos"));
        }
    }

    /**
     * Obtener resumen de movimientos para un producto.
     * Estadísticas de entradas, salidas y stock actual.
     *
     * @param idProducto ID del producto
     * @param fechaInicio Fecha inicial (opcional)
     * @param fechaFin Fecha final (opcional)
     * @return Resumen estadístico
     */
    @GetMapping("/producto/{idProducto}/resumen")
    @Operation(
            summary = "Resumen de movimientos",
            description = "Obtiene estadísticas de entradas, salidas y movimientos totales de un producto"
    )
    public ResponseEntity<?> obtenerResumenProducto(
            @PathVariable Long idProducto,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        try {
            log.info("GET /api/inventario/producto/{}/resumen", idProducto);

            // Si no se especifican fechas, usar mes actual
            if (fechaInicio == null) {
                fechaInicio = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            }
            if (fechaFin == null) {
                fechaFin = LocalDateTime.now();
            }

            Integer totalEntradas = inventarioService.obtenerTotalEntradas(idProducto, fechaInicio, fechaFin);
            Integer totalSalidas = inventarioService.obtenerTotalSalidas(idProducto, fechaInicio, fechaFin);

            Map<String, Object> resumen = new HashMap<>();
            resumen.put("idProducto", idProducto);
            resumen.put("totalEntradas", totalEntradas != null ? totalEntradas : 0);
            resumen.put("totalSalidas", totalSalidas != null ? totalSalidas : 0);
            resumen.put("movimientoNeto", (totalEntradas != null ? totalEntradas : 0) - (totalSalidas != null ? totalSalidas : 0));
            resumen.put("fechaInicio", fechaInicio);
            resumen.put("fechaFin", fechaFin);

            return ResponseEntity.ok(resumen);

        } catch (Exception e) {
            log.error("Error al obtener resumen del producto {}: {}", idProducto, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener resumen"));
        }
    }
}