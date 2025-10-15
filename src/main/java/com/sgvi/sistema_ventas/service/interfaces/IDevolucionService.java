package com.sgvi.sistema_ventas.service.interfaces;

import com.sgvi.sistema_ventas.model.entity.DetalleDevolucion;
import com.sgvi.sistema_ventas.model.entity.Devolucion;
import com.sgvi.sistema_ventas.model.enums.EstadoDevolucion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interfaz de servicio para la gestión de devoluciones.
 * Define los contratos según RF-013: Gestión de Devoluciones
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public interface IDevolucionService {

    /**
     * RF-013: Crear solicitud de devolución
     * @param devolucion Devolución a crear
     * @param detalles Lista de productos a devolver
     * @return Devolución creada
     */
    Devolucion crearDevolucion(Devolucion devolucion, List<DetalleDevolucion> detalles);

    /**
     * RF-013: Aprobar devolución
     * @param idDevolucion ID de la devolución
     * @param idUsuario ID del usuario que aprueba
     * @return Devolución aprobada
     */
    Devolucion aprobarDevolucion(Long idDevolucion, Long idUsuario);

    /**
     * RF-013: Rechazar devolución
     * @param idDevolucion ID de la devolución
     * @param idUsuario ID del usuario que rechaza
     * @param motivo Motivo del rechazo
     * @return Devolución rechazada
     */
    Devolucion rechazarDevolucion(Long idDevolucion, Long idUsuario, String motivo);

    /**
     * RF-013: Completar devolución (actualizar stock y procesar reembolso)
     * @param idDevolucion ID de la devolución
     * @param idUsuario ID del usuario que completa
     * @return Devolución completada
     */
    Devolucion completarDevolucion(Long idDevolucion, Long idUsuario);

    /**
     * RF-013: Obtener devolución por ID
     * @param id ID de la devolución
     * @return Devolución encontrada
     */
    Devolucion obtenerPorId(Long id);

    /**
     * RF-013: Listar devoluciones por venta
     * @param idVenta ID de la venta
     * @return Lista de devoluciones
     */
    List<Devolucion> obtenerPorVenta(Long idVenta);

    /**
     * RF-013: Buscar devoluciones con filtros
     * @param idVenta ID de la venta (opcional)
     * @param idCliente ID del cliente (opcional)
     * @param idUsuario ID del usuario (opcional)
     * @param estado Estado de la devolución (opcional)
     * @param fechaInicio Fecha inicial (opcional)
     * @param fechaFin Fecha final (opcional)
     * @param pageable Parámetros de paginación
     * @return Página de devoluciones filtradas
     */
    Page<Devolucion> buscarConFiltros(Long idVenta, Long idCliente, Long idUsuario,
                                      EstadoDevolucion estado, LocalDateTime fechaInicio,
                                      LocalDateTime fechaFin, Pageable pageable);

    /**
     * RF-013: Verificar si una venta está dentro del plazo para devolución (30 días)
     * @param idVenta ID de la venta
     * @return true si está dentro del plazo
     */
    boolean estaDentroPlazo(Long idVenta);

    /**
     * RF-013: Validar cantidades devueltas (no exceder cantidad vendida)
     * @param idVenta ID de la venta
     * @param idProducto ID del producto
     * @param cantidadDevolver Cantidad a devolver
     * @return true si la cantidad es válida
     */
    boolean validarCantidadDevolucion(Long idVenta, Long idProducto, Integer cantidadDevolver);

    /**
     * RF-014: Obtener devoluciones por período
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Lista de devoluciones
     */
    List<Devolucion> obtenerDevolucionesPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * RF-014: Análisis de motivos de devolución
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @return Map con motivo y cantidad
     */
    Map<String, Long> analizarMotivosDevoluciones(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * RF-014: Productos más devueltos
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @param limite Límite de resultados
     * @return Lista de productos con cantidad de devoluciones
     */
    List<Map<String, Object>> obtenerProductosMasDevueltos(LocalDateTime fechaInicio,
                                                           LocalDateTime fechaFin, int limite);
}