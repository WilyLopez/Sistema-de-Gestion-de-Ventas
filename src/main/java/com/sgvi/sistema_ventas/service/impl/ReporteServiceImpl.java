package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.exception.ResourceNotFoundException;
import com.sgvi.sistema_ventas.model.entity.*;
import com.sgvi.sistema_ventas.repository.*;
import com.sgvi.sistema_ventas.service.interfaces.IReporteService;
import com.sgvi.sistema_ventas.util.excel.*;
import com.sgvi.sistema_ventas.util.pdf.ComprobanteGenerator;
import com.sgvi.sistema_ventas.util.pdf.ReportePdfGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de reportes.
 * RF-014: Generación de Reportes en Excel y PDF
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteServiceImpl implements IReporteService {

    // Repositorios
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final DevolucionRepository devolucionRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    // Exportadores Excel
    private final VentaExcelExporter ventaExcelExporter;
    private final ProductoExcelExporter productoExcelExporter;
    private final StockExcelExporter stockExcelExporter;
    private final InventarioExcelExporter inventarioExcelExporter;
    private final DevolucionExcelExporter devolucionExcelExporter;

    // Generadores PDF
    private final ComprobanteGenerator comprobanteGenerator;
    private final ReportePdfGenerator reportePdfGenerator;

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generarReporteVentasExcel(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.info("Generando reporte de ventas en Excel del {} al {}", fechaInicio, fechaFin);

        try {
            List<Venta> ventas = ventaRepository.findByFechaCreacionBetween(fechaInicio, fechaFin);

            if (ventas.isEmpty()) {
                log.warn("No se encontraron ventas en el rango de fechas especificado");
            }

            return ventaExcelExporter.exportar(ventas);
        } catch (IOException e) {
            log.error("Error al generar reporte de ventas en Excel", e);
            throw new RuntimeException("Error al generar reporte de ventas: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generarReporteStockExcel() {
        log.info("Generando reporte de stock en Excel");

        try {
            List<Producto> productos = productoRepository.findByEstadoTrue();
            return stockExcelExporter.exportar(productos, false);
        } catch (IOException e) {
            log.error("Error al generar reporte de stock en Excel", e);
            throw new RuntimeException("Error al generar reporte de stock: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generarReporteStockBajoExcel() {
        log.info("Generando reporte de productos con stock bajo en Excel");

        try {
            // Consulta productos donde Stock <= StockMinimo
            List<Producto> productosStockBajo = productoRepository
                    .findProductosConStockBajo();

            if (productosStockBajo.isEmpty()) {
                log.info("No hay productos con stock bajo");
            }

            return stockExcelExporter.exportar(productosStockBajo, true);
        } catch (IOException e) {
            log.error("Error al generar reporte de stock bajo en Excel", e);
            throw new RuntimeException("Error al generar reporte de stock bajo: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generarReporteMovimientosExcel(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.info("Generando reporte de movimientos de inventario en Excel del {} al {}",
                fechaInicio, fechaFin);

        try {
            List<Inventario> movimientos = inventarioRepository
                    .findByFechaMovimientoBetween(fechaInicio, fechaFin);

            if (movimientos.isEmpty()) {
                log.warn("No se encontraron movimientos en el rango de fechas especificado");
            }

            return inventarioExcelExporter.exportar(movimientos);
        } catch (IOException e) {
            log.error("Error al generar reporte de movimientos en Excel", e);
            throw new RuntimeException("Error al generar reporte de movimientos: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generarReporteDevolucionesExcel(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.info("Generando reporte de devoluciones en Excel del {} al {}", fechaInicio, fechaFin);

        try {
            List<Devolucion> devoluciones = devolucionRepository
                    .findByFechaDevolucionBetween(fechaInicio, fechaFin);

            if (devoluciones.isEmpty()) {
                log.warn("No se encontraron devoluciones en el rango de fechas especificado");
            }

            return devolucionExcelExporter.exportar(devoluciones);
        } catch (IOException e) {
            log.error("Error al generar reporte de devoluciones en Excel", e);
            throw new RuntimeException("Error al generar reporte de devoluciones: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerDatosDashboard() {
        log.info("Obteniendo datos del dashboard");

        Map<String, Object> datos = new HashMap<>();

        try {
            // Fecha actual
            LocalDateTime hoy = LocalDateTime.now();
            LocalDateTime inicioHoy = hoy.toLocalDate().atStartOfDay();
            LocalDateTime finHoy = hoy.toLocalDate().atTime(23, 59, 59);

            // KPI 1: Ventas de hoy
            List<Venta> ventasHoy = ventaRepository.findByFechaCreacionBetween(inicioHoy, finHoy);
            BigDecimal totalVentasHoy = ventasHoy.stream()
                    .map(Venta::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            datos.put("ventasHoy", ventasHoy.size());
            datos.put("montoVentasHoy", totalVentasHoy);

            // KPI 2: Stock crítico (productos con stock agotado o menor igual a 2)
            Long stockCritico = productoRepository.countByEstadoTrueAndStockLessThanEqual(2);
            datos.put("productosStockCritico", stockCritico);

            // KPI 3: Alertas pendientes
            Long alertasPendientes = productoRepository.countByEstadoTrueAndStockLessThanEqual(5);
            datos.put("alertasPendientes", alertasPendientes);

            // KPI 4: Top 5 productos más vendidos del mes
            LocalDateTime inicioMes = hoy.withDayOfMonth(1).toLocalDate().atStartOfDay();
            List<Map<String, Object>> topProductos = obtenerTopProductos(inicioMes, hoy, 5);
            datos.put("topProductosMes", topProductos);

            // KPI 5: Ventas por categoría del mes
            List<Map<String, Object>> ventasPorCategoria = obtenerVentasPorCategoria(inicioMes, hoy);
            datos.put("ventasPorCategoria", ventasPorCategoria);

            // KPI 6: Total de productos activos
            Long totalProductos = productoRepository.countByEstadoTrue();
            datos.put("totalProductos", totalProductos);

            // KPI 7: Ventas del mes
            List<Venta> ventasMes = ventaRepository.findByFechaCreacionBetween(inicioMes, hoy);
            BigDecimal totalVentasMes = ventasMes.stream()
                    .map(Venta::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            datos.put("ventasMes", ventasMes.size());
            datos.put("montoVentasMes", totalVentasMes);

            // KPI 8: Promedio de venta
            BigDecimal promedioVenta = ventasMes.isEmpty() ? BigDecimal.ZERO :
                    totalVentasMes.divide(BigDecimal.valueOf(ventasMes.size()), 2,
                            java.math.RoundingMode.HALF_UP);
            datos.put("promedioVenta", promedioVenta);

            log.info("Datos del dashboard obtenidos exitosamente");
            return datos;

        } catch (Exception e) {
            log.error("Error al obtener datos del dashboard", e);
            throw new RuntimeException("Error al obtener datos del dashboard: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerVentasPorCategoria(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.info("Obteniendo ventas por categoría del {} al {}", fechaInicio, fechaFin);

        try {
            // Obtener todas las ventas del período
            List<Venta> ventas = ventaRepository.findByFechaCreacionBetween(fechaInicio, fechaFin);

            // Agrupar por categoría y sumar totales
            Map<String, BigDecimal> ventasPorCategoria = new HashMap<>();

            for (Venta venta : ventas) {
                for (DetalleVenta detalle : venta.getDetallesVenta()) {
                    String categoria = detalle.getProducto().getCategoria().getNombre();
                    BigDecimal subtotal = detalle.getSubtotal();

                    ventasPorCategoria.merge(categoria, subtotal, BigDecimal::add);
                }
            }

            // Convertir a lista de mapas para retornar
            List<Map<String, Object>> resultado = new ArrayList<>();
            for (Map.Entry<String, BigDecimal> entry : ventasPorCategoria.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("categoria", entry.getKey());
                item.put("total", entry.getValue());
                resultado.add(item);
            }

            // Ordenar por total descendente
            resultado.sort((a, b) ->
                    ((BigDecimal) b.get("total")).compareTo((BigDecimal) a.get("total")));

            log.info("Se encontraron {} categorías con ventas", resultado.size());
            return resultado;

        } catch (Exception e) {
            log.error("Error al obtener ventas por categoría", e);
            throw new RuntimeException("Error al obtener ventas por categoría: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerTopProductos(LocalDateTime fechaInicio,
                                                         LocalDateTime fechaFin,
                                                         int limite) {
        log.info("Obteniendo top {} productos del {} al {}", limite, fechaInicio, fechaFin);

        try {
            // Obtener todas las ventas del período
            List<Venta> ventas = ventaRepository.findByFechaCreacionBetween(fechaInicio, fechaFin);

            // Agrupar por producto y sumar cantidades
            Map<Producto, Integer> productosCantidad = new HashMap<>();
            Map<Producto, BigDecimal> productosTotal = new HashMap<>();

            for (Venta venta : ventas) {
                for (DetalleVenta detalle : venta.getDetallesVenta()) {
                    Producto producto = detalle.getProducto();

                    productosCantidad.merge(producto, detalle.getCantidad(), Integer::sum);
                    productosTotal.merge(producto, detalle.getSubtotal(), BigDecimal::add);
                }
            }

            // Convertir a lista y ordenar por cantidad
            List<Map<String, Object>> resultado = productosCantidad.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> item = new HashMap<>();
                        Producto producto = entry.getKey();
                        item.put("idProducto", producto.getIdProducto());
                        item.put("codigo", producto.getCodigo());
                        item.put("nombre", producto.getNombre());
                        item.put("marca", producto.getMarca());
                        item.put("categoria", producto.getCategoria().getNombre());
                        item.put("cantidadVendida", entry.getValue());
                        item.put("totalVentas", productosTotal.get(producto));
                        return item;
                    })
                    .sorted((a, b) ->
                            ((Integer) b.get("cantidadVendida")).compareTo((Integer) a.get("cantidadVendida")))
                    .limit(limite)
                    .collect(Collectors.toList());

            log.info("Se encontraron {} productos vendidos", resultado.size());
            return resultado;

        } catch (Exception e) {
            log.error("Error al obtener top productos", e);
            throw new RuntimeException("Error al obtener top productos: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generarComprobantePDF(Long idVenta) {
        log.info("Generando comprobante PDF para venta ID: {}", idVenta);

        try {
            // Buscar venta con sus relaciones
            Venta venta = ventaRepository.findById(idVenta)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Venta", "id", idVenta));

            // Generar PDF
            return comprobanteGenerator.generarComprobante(venta);

        } catch (ResourceNotFoundException e) {
            log.error("Venta no encontrada: {}", idVenta);
            throw e;
        } catch (IOException e) {
            log.error("Error al generar comprobante PDF para venta ID: {}", idVenta, e);
            throw new RuntimeException("Error al generar comprobante PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Genera reporte de ventas en PDF (adicional)
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return ByteArrayOutputStream con el PDF
     */
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generarReporteVentasPDF(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.info("Generando reporte de ventas en PDF del {} al {}", fechaInicio, fechaFin);

        try {
            List<Venta> ventas = ventaRepository.findByFechaCreacionBetween(fechaInicio, fechaFin);

            if (ventas.isEmpty()) {
                log.warn("No se encontraron ventas en el rango de fechas especificado");
            }

            return reportePdfGenerator.generarReporteVentas(ventas, fechaInicio, fechaFin);

        } catch (IOException e) {
            log.error("Error al generar reporte de ventas en PDF", e);
            throw new RuntimeException("Error al generar reporte de ventas PDF: " + e.getMessage(), e);
        }
    }
}