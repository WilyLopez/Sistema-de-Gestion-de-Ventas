package com.sgvi.sistema_ventas.service.interfaces;

import com.sgvi.sistema_ventas.model.dto.venta.VentaBusquedaDTO;
import com.sgvi.sistema_ventas.model.dto.venta.VentaDTO;
import com.sgvi.sistema_ventas.model.entity.DetalleVenta;
import com.sgvi.sistema_ventas.model.entity.Venta;
import com.sgvi.sistema_ventas.model.enums.EstadoVenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz de servicio para la gestión de ventas.
 * Define los contratos según RF-007, RF-008, RF-009
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public interface IVentaService {

    /**
     * RF-007: Registrar una nueva venta
     * @param venta Venta a registrar
     * @param detalles Lista de detalles de la venta
     * @return Venta creada y procesada
     */
    Venta registrarVenta(Venta venta, List<DetalleVenta> detalles);

    /**
     * RF-008: Obtener venta por ID
     * @param id ID de la venta
     * @return Venta encontrada
     */
    Venta obtenerPorId(Long id);

    /**
     * RF-008: Obtener venta por código
     * @param codigoVenta Código de la venta
     * @return Venta encontrada
     */
    Venta obtenerPorCodigo(String codigoVenta);

    /**
     * RF-008: Listar todas las ventas
     * @param pageable Parámetros de paginación
     * @return Página de ventas
     */
    Page<Venta> listarTodas(Pageable pageable);


    Page<VentaDTO> buscarVentasDTOConFiltros(VentaBusquedaDTO filtros);

    /**
     * RF-009: Anular una venta
     * @param idVenta ID de la venta a anular
     * @param motivo Motivo de la anulación
     */
    void anularVenta(Long idVenta, String motivo);

    /**
     * RF-009: Verificar si una venta puede ser anulada
     * @param idVenta ID de la venta
     * @return true si puede ser anulada (< 24 horas y estado PAGADO)
     */
    boolean puedeAnularse(Long idVenta);

    /**
     * RF-007: Calcular totales de la venta (subtotal, IGV, total)
     * @param detalles Lista de detalles de la venta
     * @return Array con [subtotal, igv, total]
     */
    BigDecimal[] calcularTotales(List<DetalleVenta> detalles);

    /**
     * RF-007: Generar código único para la venta
     * @return Código generado (ej: V-2024-00001)
     */
    String generarCodigoVenta();

    /**
     * RF-014: Obtener ventas por período
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Lista de ventas en el período
     */
    List<Venta> obtenerVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * RF-014: Obtener total de ventas por período
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Monto total vendido
     */
    BigDecimal obtenerTotalVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * RF-014: Contar ventas por período
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Cantidad de ventas
     */
    Long contarVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Obtiene estadísticas de ventas para un vendedor específico en un período.
     * @param idUsuario ID del vendedor
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Mapa con "totalVendido" y "cantidadVentas"
     */
    java.util.Map<String, Object> obtenerEstadisticasVendedor(Long idUsuario, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
