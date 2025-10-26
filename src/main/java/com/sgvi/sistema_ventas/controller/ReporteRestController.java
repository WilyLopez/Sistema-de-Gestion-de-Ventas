package com.sgvi.sistema_ventas.controller;

import com.sgvi.sistema_ventas.model.dto.auth.MessageResponse;
import com.sgvi.sistema_ventas.service.interfaces.IReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para generación de reportes y exportación de datos.
 * Implementa requisito RF-014 del SRS: Generación de Reportes.
 *
 * Funcionalidades:
 * - Exportación de reportes en Excel (.xlsx)
 * - Generación de comprobantes en PDF
 * - Consultas de KPIs y métricas para dashboard
 * - Análisis de ventas y estadísticas
 *
 * @author Wilian Lopez
 * @version 1.1
 * @since 2024
 */
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
@Tag(name = "Reportes", description = "Endpoints para generación de reportes en Excel, PDF y datos de dashboard")
public class ReporteRestController {

    private final IReporteService reporteService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * RF-014: Generar reporte de ventas en formato Excel.
     * Incluye detalles de todas las ventas en el período especificado con información
     * de cliente, productos, totales y métodos de pago.
     *
     * @param fechaInicio Fecha inicial del período (formato ISO 8601)
     * @param fechaFin Fecha final del período (formato ISO 8601)
     * @return Archivo Excel (.xlsx) con reporte de ventas
     */
    @GetMapping("/ventas/excel")
    @Operation(
            summary = "Reporte de ventas Excel",
            description = "Genera reporte detallado de ventas en formato Excel (.xlsx) con información completa de transacciones, clientes y productos"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte generado exitosamente",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Rango de fechas inválido o parámetros incorrectos",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno al generar el reporte",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<?> generarReporteVentasExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        try {
            log.info("GET /api/reportes/ventas/excel - Período: {} a {}", fechaInicio, fechaFin);

            // Validación de rango de fechas
            if (fechaInicio.isAfter(fechaFin)) {
                log.warn("Rango de fechas inválido: fechaInicio={} > fechaFin={}", fechaInicio, fechaFin);
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("La fecha de inicio no puede ser posterior a la fecha fin"));
            }

            ByteArrayOutputStream outputStream = reporteService.generarReporteVentasExcel(fechaInicio, fechaFin);

            String filename = String.format("reporte_ventas_%s.xlsx",
                    LocalDateTime.now().format(FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("no-cache, no-store, must-revalidate");

            log.info("Reporte de ventas generado exitosamente: {} bytes - {}", outputStream.size(), filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (IllegalArgumentException e) {
            log.warn("Parámetros inválidos para reporte de ventas: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Parámetros inválidos: " + e.getMessage()));

        } catch (Exception e) {
            log.error("Error al generar reporte de ventas: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al generar reporte de ventas. Por favor intente nuevamente."));
        }
    }

    /**
     * RF-014: Generar reporte de inventario/stock actual en Excel.
     * Lista todos los productos con su stock actual, mínimo, máximo y estado.
     *
     * @return Archivo Excel con reporte de inventario completo
     */
    @GetMapping("/stock/excel")
    @Operation(
            summary = "Reporte de stock Excel",
            description = "Genera reporte de inventario actual con stock disponible, mínimo, máximo y valorización"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte generado exitosamente",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error al generar reporte",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            )
    })
    public ResponseEntity<?> generarReporteStockExcel() {
        try {
            log.info("GET /api/reportes/stock/excel - Generar reporte de inventario");

            ByteArrayOutputStream outputStream = reporteService.generarReporteStockExcel();

            String filename = String.format("reporte_stock_%s.xlsx",
                    LocalDateTime.now().format(FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("no-cache, no-store, must-revalidate");

            log.info("Reporte de stock generado exitosamente: {}", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            log.error("Error al generar reporte de stock: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al generar reporte de stock"));
        }
    }

    /**
     * RF-014: Generar reporte de productos con stock bajo en Excel.
     * Filtra productos cuyo stock actual es menor o igual al stock mínimo configurado.
     *
     * @return Archivo Excel con productos que requieren reabastecimiento
     */
    @GetMapping("/stock-bajo/excel")
    @Operation(
            summary = "Reporte de stock bajo Excel",
            description = "Genera reporte de productos con stock menor o igual al mínimo establecido. Útil para órdenes de compra"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte generado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error al generar reporte"
            )
    })
    public ResponseEntity<?> generarReporteStockBajoExcel() {
        try {
            log.info("GET /api/reportes/stock-bajo/excel - Generar reporte de productos con stock bajo");

            ByteArrayOutputStream outputStream = reporteService.generarReporteStockBajoExcel();

            String filename = String.format("reporte_stock_bajo_%s.xlsx",
                    LocalDateTime.now().format(FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("no-cache, no-store, must-revalidate");

            log.info("Reporte de stock bajo generado: {}", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            log.error("Error al generar reporte de stock bajo: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al generar reporte de stock bajo"));
        }
    }

    /**
     * RF-014: Generar reporte de movimientos de inventario en Excel.
     * Incluye entradas, salidas, ajustes y transferencias de inventario.
     *
     * @param fechaInicio Fecha inicial del período
     * @param fechaFin Fecha final del período
     * @return Archivo Excel con historial de movimientos
     */
    @GetMapping("/movimientos/excel")
    @Operation(
            summary = "Reporte de movimientos Excel",
            description = "Genera reporte detallado de movimientos de inventario: entradas, salidas, ajustes y transferencias"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte generado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Rango de fechas inválido",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error al generar reporte"
            )
    })
    public ResponseEntity<?> generarReporteMovimientosExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        try {
            log.info("GET /api/reportes/movimientos/excel - Período: {} a {}", fechaInicio, fechaFin);

            if (fechaInicio.isAfter(fechaFin)) {
                log.warn("Rango de fechas inválido para movimientos");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("La fecha de inicio no puede ser posterior a la fecha fin"));
            }

            ByteArrayOutputStream outputStream = reporteService.generarReporteMovimientosExcel(
                    fechaInicio, fechaFin);

            String filename = String.format("reporte_movimientos_%s.xlsx",
                    LocalDateTime.now().format(FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("no-cache, no-store, must-revalidate");

            log.info("Reporte de movimientos generado: {}", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (IllegalArgumentException e) {
            log.warn("Parámetros inválidos: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al generar reporte de movimientos: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al generar reporte de movimientos"));
        }
    }

    /**
     * RF-014: Generar reporte de devoluciones en Excel.
     * Solo accesible para administradores. Incluye detalles de devoluciones procesadas.
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Archivo Excel con reporte de devoluciones
     */
    @GetMapping("/devoluciones/excel")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Reporte de devoluciones Excel",
            description = "Genera reporte de devoluciones procesadas. Requiere rol ADMINISTRADOR"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte generado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Rango de fechas inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sin permisos suficientes"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error al generar reporte"
            )
    })
    public ResponseEntity<?> generarReporteDevolucionesExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        try {
            log.info("GET /api/reportes/devoluciones/excel - Período: {} a {}", fechaInicio, fechaFin);

            if (fechaInicio.isAfter(fechaFin)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("La fecha de inicio no puede ser posterior a la fecha fin"));
            }

            ByteArrayOutputStream outputStream = reporteService.generarReporteDevolucionesExcel(
                    fechaInicio, fechaFin);

            String filename = String.format("reporte_devoluciones_%s.xlsx",
                    LocalDateTime.now().format(FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("no-cache, no-store, must-revalidate");

            log.info("Reporte de devoluciones generado: {}", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("Error al generar reporte de devoluciones: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al generar reporte de devoluciones"));
        }
    }

    /**
     * RF-014: Generar comprobante de venta en PDF.
     * Genera documento PDF con formato de comprobante oficial.
     *
     * @param idVenta ID de la venta
     * @return Archivo PDF con comprobante
     */
    @GetMapping("/comprobante/{idVenta}/pdf")
    @Operation(
            summary = "Generar comprobante PDF",
            description = "Genera comprobante de venta en formato PDF con información oficial de la transacción"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Comprobante generado exitosamente",
                    content = @Content(mediaType = "application/pdf")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Venta no encontrada",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error al generar comprobante"
            )
    })
    public ResponseEntity<?> generarComprobantePDF(@PathVariable Long idVenta) {
        try {
            log.info("GET /api/reportes/comprobante/{}/pdf - Generar comprobante", idVenta);

            if (idVenta == null || idVenta <= 0) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("ID de venta inválido"));
            }

            ByteArrayOutputStream outputStream = reporteService.generarComprobantePDF(idVenta);

            String filename = String.format("comprobante_venta_%d_%s.pdf",
                    idVenta, LocalDateTime.now().format(FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("no-cache, no-store, must-revalidate");

            log.info("Comprobante PDF generado: {}", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (IllegalArgumentException e) {
            log.warn("Venta no encontrada: ID {}", idVenta);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Venta no encontrada con ID: " + idVenta));

        } catch (Exception e) {
            log.error("Error al generar comprobante PDF para venta {}: {}", idVenta, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al generar comprobante PDF"));
        }
    }

    /**
     * RF-014: Obtener datos para dashboard principal.
     * Retorna KPIs, métricas y estadísticas clave del sistema.
     *
     * @return Mapa con datos agregados del dashboard
     */
    @GetMapping("/dashboard")
    @Operation(
            summary = "Datos del dashboard",
            description = "Obtiene KPIs, métricas y datos agregados para el dashboard principal: ventas del día, productos bajo stock, alertas, etc."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Datos obtenidos exitosamente"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error al obtener datos"
            )
    })
    public ResponseEntity<?> obtenerDatosDashboard() {
        try {
            log.info("GET /api/reportes/dashboard - Obtener KPIs y métricas");

            Map<String, Object> datos = reporteService.obtenerDatosDashboard();

            // Agregar timestamp para cache invalidation en frontend
            datos.put("timestamp", LocalDateTime.now());

            log.debug("Datos del dashboard obtenidos exitosamente");

            return ResponseEntity.ok(datos);

        } catch (Exception e) {
            log.error("Error al obtener datos del dashboard: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener datos del dashboard"));
        }
    }

    /**
     * RF-014: Análisis de ventas por categoría de producto.
     * Agrupa y totaliza ventas según la categoría de productos.
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Lista con análisis por categoría
     */
    @GetMapping("/ventas-por-categoria")
    @Operation(
            summary = "Ventas por categoría",
            description = "Análisis de ventas agrupadas por categoría de producto con totales y porcentajes"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Análisis obtenido exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Rango de fechas inválido"
            )
    })
    public ResponseEntity<?> obtenerVentasPorCategoria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        try {
            log.info("GET /api/reportes/ventas-por-categoria - Análisis del {} al {}", fechaInicio, fechaFin);

            if (fechaInicio.isAfter(fechaFin)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("La fecha de inicio no puede ser posterior a la fecha fin"));
            }

            List<Map<String, Object>> datos = reporteService.obtenerVentasPorCategoria(
                    fechaInicio, fechaFin);

            log.debug("Análisis por categoría completado: {} categorías", datos.size());

            return ResponseEntity.ok(datos);

        } catch (Exception e) {
            log.error("Error al obtener ventas por categoría: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener análisis por categoría"));
        }
    }

    /**
     * RF-014: Top productos más vendidos.
     * Lista los productos con mayor cantidad de ventas en el período.
     *
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @param limite Cantidad de productos a retornar (default: 10)
     * @return Lista ordenada de productos más vendidos
     */
    @GetMapping("/top-productos")
    @Operation(
            summary = "Top productos vendidos",
            description = "Lista los productos más vendidos ordenados por cantidad o monto total. Útil para análisis de desempeño"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Top productos obtenido exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetros inválidos"
            )
    })
    public ResponseEntity<?> obtenerTopProductos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "10") int limite) {

        try {
            log.info("GET /api/reportes/top-productos - Top {} productos del {} al {}",
                    limite, fechaInicio, fechaFin);

            if (fechaInicio.isAfter(fechaFin)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("La fecha de inicio no puede ser posterior a la fecha fin"));
            }

            if (limite <= 0 || limite > 100) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("El límite debe estar entre 1 y 100"));
            }

            List<Map<String, Object>> productos = reporteService.obtenerTopProductos(
                    fechaInicio, fechaFin, limite);

            log.debug("Top productos obtenido: {} productos", productos.size());

            return ResponseEntity.ok(productos);

        } catch (Exception e) {
            log.error("Error al obtener top productos: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error al obtener top productos"));
        }
    }

    /**
     * Endpoint de prueba para verificar el servicio de reportes.
     * Útil para health checks y debugging.
     *
     * @return Estado del servicio
     */
    @GetMapping("/test")
    @Operation(
            summary = "Test de servicio",
            description = "Endpoint de prueba para verificar que el servicio de reportes está funcionando correctamente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Servicio funcionando correctamente"
            )
    })
    public ResponseEntity<Map<String, Object>> testReportes() {
        log.info("GET /api/reportes/test - Verificando servicio de reportes");

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("estado", "OK");
        respuesta.put("mensaje", "Servicio de reportes funcionando correctamente");
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("version", "1.1");
        respuesta.put("endpoints", Map.of(
                "ventas", "/api/reportes/ventas/excel",
                "stock", "/api/reportes/stock/excel",
                "dashboard", "/api/reportes/dashboard",
                "comprobante", "/api/reportes/comprobante/{id}/pdf"
        ));

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtener resumen de reportes disponibles.
     * Útil para mostrar opciones de reportes en el frontend.
     *
     * @return Lista de reportes disponibles con metadatos
     */
    @GetMapping("/disponibles")
    @Operation(
            summary = "Reportes disponibles",
            description = "Lista todos los tipos de reportes disponibles con sus descripciones y parámetros"
    )
    public ResponseEntity<List<Map<String, Object>>> obtenerReportesDisponibles() {
        try {
            log.info("GET /api/reportes/disponibles");

            List<Map<String, Object>> reportes = List.of(
                    Map.of(
                            "nombre", "Ventas",
                            "descripcion", "Reporte detallado de ventas",
                            "formato", "Excel",
                            "requiereFechas", true,
                            "endpoint", "/api/reportes/ventas/excel"
                    ),
                    Map.of(
                            "nombre", "Stock Actual",
                            "descripcion", "Inventario completo con stock actual",
                            "formato", "Excel",
                            "requiereFechas", false,
                            "endpoint", "/api/reportes/stock/excel"
                    ),
                    Map.of(
                            "nombre", "Stock Bajo",
                            "descripcion", "Productos que requieren reabastecimiento",
                            "formato", "Excel",
                            "requiereFechas", false,
                            "endpoint", "/api/reportes/stock-bajo/excel"
                    ),
                    Map.of(
                            "nombre", "Movimientos",
                            "descripcion", "Historial de movimientos de inventario",
                            "formato", "Excel",
                            "requiereFechas", true,
                            "endpoint", "/api/reportes/movimientos/excel"
                    ),
                    Map.of(
                            "nombre", "Devoluciones",
                            "descripcion", "Reporte de devoluciones procesadas",
                            "formato", "Excel",
                            "requiereFechas", true,
                            "rolesPermitidos", List.of("ADMINISTRADOR"),
                            "endpoint", "/api/reportes/devoluciones/excel"
                    )
            );

            return ResponseEntity.ok(reportes);

        } catch (Exception e) {
            log.error("Error al obtener reportes disponibles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}