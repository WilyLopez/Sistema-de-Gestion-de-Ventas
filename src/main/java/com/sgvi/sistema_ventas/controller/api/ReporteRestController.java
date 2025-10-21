package com.sgvi.sistema_ventas.controller.api;

import com.sgvi.sistema_ventas.service.interfaces.IReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Controller REST para generación de reportes.
 * RF-014: Generación de Reportes
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
@Tag(name = "Reportes", description = "Endpoints para generación de reportes y exportación")
public class ReporteRestController {

    private final IReporteService reporteService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * RF-014: Generar reporte de ventas en Excel
     * GET /api/reportes/ventas/excel
     */
    @GetMapping("/ventas/excel")
    @Operation(summary = "Reporte de ventas Excel", description = "Genera reporte de ventas en formato Excel")
    public ResponseEntity<byte[]> generarReporteVentasExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        log.info("GET /api/reportes/ventas/excel - Generar reporte de ventas");

        try {
            ByteArrayOutputStream outputStream = reporteService.generarReporteVentasExcel(fechaInicio, fechaFin);

            String filename = String.format("reporte_ventas_%s.xlsx",
                    LocalDateTime.now().format(FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            log.error("Error al generar reporte de ventas: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * RF-014: Generar reporte de stock en Excel
     * GET /api/reportes/stock/excel
     */
    @GetMapping("/stock/excel")
    @Operation(summary = "Reporte de stock Excel", description = "Genera reporte de stock actual en formato Excel")
    public ResponseEntity<byte[]> generarReporteStockExcel() {
        log.info("GET /api/reportes/stock/excel - Generar reporte de stock");

        try {
            ByteArrayOutputStream outputStream = reporteService.generarReporteStockExcel();

            String filename = String.format("reporte_stock_%s.xlsx",
                    LocalDateTime.now().format(FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            log.error("Error al generar reporte de stock: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * RF-014: Generar reporte de stock bajo en Excel
     * GET /api/reportes/stock-bajo/excel
     */
    @GetMapping("/stock-bajo/excel")
    @Operation(summary = "Reporte de stock bajo Excel", description = "Genera reporte de productos con stock bajo")
    public ResponseEntity<byte[]> generarReporteStockBajoExcel() {
        log.info("GET /api/reportes/stock-bajo/excel - Generar reporte de stock bajo");

        try {
            ByteArrayOutputStream outputStream = reporteService.generarReporteStockBajoExcel();

            String filename = String.format("reporte_stock_bajo_%s.xlsx",
                    LocalDateTime.now().format(FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            log.error("Error al generar reporte de stock bajo: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * RF-014: Generar reporte de movimientos de inventario en Excel
     * GET /api/reportes/movimientos/excel
     */
    @GetMapping("/movimientos/excel")
    @Operation(summary = "Reporte de movimientos Excel", description = "Genera reporte de movimientos de inventario")
    public ResponseEntity<byte[]> generarReporteMovimientosExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        log.info("GET /api/reportes/movimientos/excel - Generar reporte de movimientos");

        try {
            ByteArrayOutputStream outputStream = reporteService.generarReporteMovimientosExcel(
                    fechaInicio, fechaFin);

            String filename = String.format("reporte_movimientos_%s.xlsx",
                    LocalDateTime.now().format(FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            log.error("Error al generar reporte de movimientos: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * RF-014: Generar reporte de devoluciones en Excel
     * GET /api/reportes/devoluciones/excel
     */
    @GetMapping("/devoluciones/excel")
    @Operation(summary = "Reporte de devoluciones Excel", description = "Genera reporte de devoluciones")
    public ResponseEntity<byte[]> generarReporteDevolucionesExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        log.info("GET /api/reportes/devoluciones/excel - Generar reporte de devoluciones");

        try {
            ByteArrayOutputStream outputStream = reporteService.generarReporteDevolucionesExcel(
                    fechaInicio, fechaFin);

            String filename = String.format("reporte_devoluciones_%s.xlsx",
                    LocalDateTime.now().format(FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            log.error("Error al generar reporte de devoluciones: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * RF-014: Obtener datos del dashboard
     * GET /api/reportes/dashboard
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Datos del dashboard", description = "Obtiene KPIs y métricas para el dashboard")
    public ResponseEntity<Map<String, Object>> obtenerDatosDashboard() {
        log.info("GET /api/reportes/dashboard - Obtener datos del dashboard");

        try {
            Map<String, Object> datos = reporteService.obtenerDatosDashboard();
            return ResponseEntity.ok(datos);
        } catch (Exception e) {
            log.error("Error al obtener datos del dashboard: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * RF-014: Obtener ventas por categoría
     * GET /api/reportes/ventas-por-categoria
     */
    @GetMapping("/ventas-por-categoria")
    @Operation(summary = "Ventas por categoría", description = "Análisis de ventas agrupadas por categoría")
    public ResponseEntity<List<Map<String, Object>>> obtenerVentasPorCategoria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        log.info("GET /api/reportes/ventas-por-categoria - Análisis por categoría");

        try {
            List<Map<String, Object>> datos = reporteService.obtenerVentasPorCategoria(
                    fechaInicio, fechaFin);
            return ResponseEntity.ok(datos);
        } catch (Exception e) {
            log.error("Error al obtener ventas por categoría: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * RF-014: Obtener top productos más vendidos
     * GET /api/reportes/top-productos
     */
    @GetMapping("/top-productos")
    @Operation(summary = "Top productos vendidos", description = "Lista los productos más vendidos")
    public ResponseEntity<List<Map<String, Object>>> obtenerTopProductos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "10") int limite) {

        log.info("GET /api/reportes/top-productos - Top {} productos", limite);

        try {
            List<Map<String, Object>> productos = reporteService.obtenerTopProductos(
                    fechaInicio, fechaFin, limite);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            log.error("Error al obtener top productos: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * RF-014: Generar comprobante en PDF
     * GET /api/reportes/comprobante/{idVenta}/pdf
     */
    @GetMapping("/comprobante/{idVenta}/pdf")
    @Operation(summary = "Generar comprobante PDF", description = "Genera comprobante de venta en formato PDF")
    public ResponseEntity<byte[]> generarComprobantePDF(@PathVariable Long idVenta) {
        log.info("GET /api/reportes/comprobante/{}/pdf - Generar comprobante", idVenta);

        try {
            ByteArrayOutputStream outputStream = reporteService.generarComprobantePDF(idVenta);

            String filename = String.format("comprobante_venta_%d.pdf", idVenta);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            log.error("Error al generar comprobante PDF: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint para probar generación de reportes
     * GET /api/reportes/test
     */
    @GetMapping("/test")
    @Operation(summary = "Test de reportes", description = "Endpoint de prueba para verificar servicio")
    public ResponseEntity<Map<String, String>> testReportes() {
        log.info("GET /api/reportes/test - Test de reportes");

        return ResponseEntity.ok(Map.of(
                "estado", "OK",
                "mensaje", "Servicio de reportes funcionando correctamente",
                "fecha", LocalDateTime.now().toString()
        ));
    }
}