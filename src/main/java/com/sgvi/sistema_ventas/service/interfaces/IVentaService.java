package com.sgvi.sistema_ventas.service.interfaces;

import com.sgvi.sistema_ventas.model.dto.common.PageResponseDTO;
import com.sgvi.sistema_ventas.model.dto.common.ResponseDTO;
import com.sgvi.sistema_ventas.model.dto.venta.*;
import com.sgvi.sistema_ventas.model.enums.EstadoVenta;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface para la gestión de ventas.
 * Define las operaciones relacionadas con ventas según RF-007, RF-008, RF-009
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public interface IVentaService {

    // RF-007: Registro de venta
    ResponseDTO<VentaDTO> registrarVenta(VentaCreateDTO ventaCreateDTO, Long idUsuario);

    // RF-008: Consulta de ventas
    ResponseDTO<PageResponseDTO<VentaResumenDTO>> obtenerVentas(VentaBusquedaDTO filtros, Pageable pageable);
    ResponseDTO<VentaDTO> obtenerVentaPorId(Long idVenta);
    ResponseDTO<VentaDTO> obtenerVentaPorCodigo(String codigoVenta);

    // RF-009: Anulación de venta
    ResponseDTO<Void> anularVenta(Long idVenta, String motivo, Long idUsuario);

    // RF-008: Búsquedas específicas
    ResponseDTO<List<VentaResumenDTO>> obtenerVentasPorCliente(Long idCliente);
    ResponseDTO<List<VentaResumenDTO>> obtenerVentasPorUsuario(Long idUsuario);
    ResponseDTO<List<VentaResumenDTO>> obtenerVentasPorEstado(EstadoVenta estado);
    ResponseDTO<List<VentaResumenDTO>> obtenerVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // RF-007: Cálculos de venta
    ResponseDTO<BigDecimal> calcularSubtotal(List<DetalleVentaDTO> detalles);
    ResponseDTO<BigDecimal> calcularIGV(BigDecimal subtotal);
    ResponseDTO<BigDecimal> calcularTotal(BigDecimal subtotal, BigDecimal igv);

    // RF-008: Generación de comprobante
    ResponseDTO<ComprobanteDTO> generarComprobante(Long idVenta);

    // RF-014: Métricas y estadísticas
    ResponseDTO<BigDecimal> obtenerTotalVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    ResponseDTO<Long> obtenerCantidadVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    ResponseDTO<List<Object[]>> obtenerVentasPorCategoria(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    ResponseDTO<List<Object[]>> obtenerTopProductosVendidos(LocalDateTime fechaInicio, LocalDateTime fechaFin, int limite);

    // Validaciones
    ResponseDTO<Boolean> validarStockSuficiente(List<DetalleVentaDTO> detalles);
    ResponseDTO<Boolean> validarVentaAnulable(Long idVenta);
}