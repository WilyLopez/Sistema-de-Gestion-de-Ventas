package com.sgvi.sistema_ventas.service.interfaces;

import com.sgvi.sistema_ventas.model.entity.Inventario;
import com.sgvi.sistema_ventas.model.enums.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz de servicio para la gestión de inventario.
 * Define los contratos según RF-012: Gestión de Inventario
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public interface IInventarioService {

    /**
     * RF-012: Registrar un movimiento de inventario
     * @param movimiento Movimiento a registrar
     * @return Movimiento registrado
     */
    Inventario registrarMovimiento(Inventario movimiento);

    /**
     * RF-012: Registrar entrada de productos (compra a proveedor)
     * @param idProducto ID del producto
     * @param cantidad Cantidad ingresada
     * @param idUsuario ID del usuario que registra
     * @param observacion Observación del movimiento
     * @return Movimiento de entrada registrado
     */
    Inventario registrarEntrada(Long idProducto, Integer cantidad, Long idUsuario, String observacion);

    /**
     * RF-012: Registrar salida de productos (venta)
     * @param idProducto ID del producto
     * @param cantidad Cantidad vendida
     * @param idUsuario ID del usuario que registra
     * @param idVenta ID de la venta asociada
     * @return Movimiento de salida registrado
     */
    Inventario registrarSalida(Long idProducto, Integer cantidad, Long idUsuario, Long idVenta);

    /**
     * RF-012: Registrar ajuste de inventario (corrección manual)
     * @param idProducto ID del producto
     * @param nuevoStock Nuevo stock después del ajuste
     * @param idUsuario ID del usuario que registra
     * @param observacion Motivo del ajuste
     * @return Movimiento de ajuste registrado
     */
    Inventario registrarAjuste(Long idProducto, Integer nuevoStock, Long idUsuario, String observacion);

    /**
     * RF-012: Registrar devolución de producto
     * @param idProducto ID del producto
     * @param cantidad Cantidad devuelta
     * @param idUsuario ID del usuario que registra
     * @param observacion Motivo de la devolución
     * @return Movimiento de devolución registrado
     */
    Inventario registrarDevolucion(Long idProducto, Integer cantidad, Long idUsuario, String observacion);

    /**
     * RF-012: Obtener movimientos por producto
     * @param idProducto ID del producto
     * @param pageable Parámetros de paginación
     * @return Página de movimientos
     */
    Page<Inventario> obtenerMovimientosPorProducto(Long idProducto, Pageable pageable);

    /**
     * RF-012: Obtener trazabilidad completa de un producto
     * @param idProducto ID del producto
     * @return Lista completa de movimientos del producto
     */
    List<Inventario> obtenerTrazabilidad(Long idProducto);

    /**
     * RF-012: Buscar movimientos con filtros
     * @param idProducto ID del producto (opcional)
     * @param tipoMovimiento Tipo de movimiento (opcional)
     * @param idUsuario ID del usuario (opcional)
     * @param fechaInicio Fecha inicial (opcional)
     * @param fechaFin Fecha final (opcional)
     * @param pageable Parámetros de paginación
     * @return Página de movimientos filtrados
     */
    Page<Inventario> buscarMovimientosConFiltros(Long idProducto, TipoMovimiento tipoMovimiento,
                                                 Long idUsuario, LocalDateTime fechaInicio,
                                                 LocalDateTime fechaFin, Pageable pageable);

    /**
     * RF-014: Obtener movimientos por período
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Lista de movimientos en el período
     */
    List<Inventario> obtenerMovimientosPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * RF-014: Obtener total de entradas por producto en período
     * @param idProducto ID del producto
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Total de entradas
     */
    Integer obtenerTotalEntradas(Long idProducto, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * RF-014: Obtener total de salidas por producto en período
     * @param idProducto ID del producto
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Total de salidas
     */
    Integer obtenerTotalSalidas(Long idProducto, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}