package com.sgvi.sistema_ventas.service.interfaces;

import com.sgvi.sistema_ventas.model.entity.DetallePedido;
import com.sgvi.sistema_ventas.model.entity.Pedido;
import com.sgvi.sistema_ventas.model.enums.EstadoPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz de servicio para la gestión de pedidos.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public interface IPedidoService {

    /**
     * Crear un nuevo pedido
     * @param pedido Pedido a crear
     * @param detalles Lista de detalles del pedido
     * @return Pedido creado
     */
    Pedido crearPedido(Pedido pedido, List<DetallePedido> detalles);

    /**
     * Actualizar estado de un pedido
     * @param idPedido ID del pedido
     * @param nuevoEstado Nuevo estado
     * @return Pedido actualizado
     */
    Pedido actualizarEstado(Long idPedido, EstadoPedido nuevoEstado);

    /**
     * Cancelar un pedido
     * @param idPedido ID del pedido
     * @param motivo Motivo de cancelación
     * @return Pedido cancelado
     */
    Pedido cancelarPedido(Long idPedido, String motivo);

    /**
     * Obtener pedido por ID
     * @param id ID del pedido
     * @return Pedido encontrado
     */
    Pedido obtenerPorId(Long id);

    /**
     * Buscar pedidos con filtros
     * @param codigoPedido Código del pedido (opcional)
     * @param idCliente ID del cliente (opcional)
     * @param idUsuario ID del usuario (opcional)
     * @param estado Estado (opcional)
     * @param fechaInicio Fecha inicial (opcional)
     * @param fechaFin Fecha final (opcional)
     * @param pageable Parámetros de paginación
     * @return Página de pedidos filtrados
     */
    Page<Pedido> buscarConFiltros(String codigoPedido, Long idCliente, Long idUsuario,
                                  EstadoPedido estado, LocalDateTime fechaInicio,
                                  LocalDateTime fechaFin, Pageable pageable);

    /**
     * Generar código único para el pedido
     * @return Código generado
     */
    String generarCodigoPedido();

    /**
     * Obtener pedidos atrasados
     * @return Lista de pedidos atrasados
     */
    List<Pedido> obtenerPedidosAtrasados();
}