package com.sgvi.sistema_ventas.service.interfaces;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interfaz de servicio para la generación de reportes.
 * Define los contratos según RF-014: Generación de Reportes
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
import java.time.LocalDate;
import java.util.Map;

/**
 * Interfaz de servicio para la generación de reportes.
 * Define los contratos según RF-014: Generación de Reportes
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public interface IReporteService {

    /**
     * RF-014: Generar reporte de ventas en Excel
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return ByteArray del archivo Excel
     */
    ByteArrayOutputStream generarReporteVentasExcel(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * RF-014: Generar reporte de stock en Excel
     * @return ByteArray del archivo Excel
     */
    ByteArrayOutputStream generarReporteStockExcel();

    /**
     * RF-014: Generar reporte de productos con stock bajo en Excel
     * @return ByteArray del archivo Excel
     */
    ByteArrayOutputStream generarReporteStockBajoExcel();

    /**
     * RF-014: Generar reporte de movimientos de inventario en Excel
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return ByteArray del archivo Excel
     */
    ByteArrayOutputStream generarReporteMovimientosExcel(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * RF-014: Generar reporte de devoluciones en Excel
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return ByteArray del archivo Excel
     */
    ByteArrayOutputStream generarReporteDevolucionesExcel(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * RF-014: Obtener datos del dashboard
     * @return Map con KPIs (ventas hoy, stock crítico, top productos, etc.)
     */
    Map<String, Object> obtenerDatosDashboard();
    
    /**
     * Genera un reporte diario de ventas para un vendedor específico.
     * @param idUsuario ID del usuario (vendedor)
     * @param fecha Fecha del reporte
     * @return Map con los datos del reporte (totalVentas, numeroTransacciones, etc.)
     */
    Map<String, Object> obtenerReporteDiarioVendedor(Long idUsuario, LocalDate fecha);

    /**
     * RF-014: Obtener ventas por categoría
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Lista con categoría y monto total
     */
    List<Map<String, Object>> obtenerVentasPorCategoria(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * RF-014: Obtener top productos más vendidos
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @param limite Número de productos a retornar
     * @return Lista de productos con cantidad vendida
     */
    List<Map<String, Object>> obtenerTopProductos(LocalDateTime fechaInicio, LocalDateTime fechaFin, int limite);

    /**
     * RF-014: Generar comprobante en PDF
     * @param idVenta ID de la venta
     * @return ByteArray del PDF
     */
    ByteArrayOutputStream generarComprobantePDF(Long idVenta);
}