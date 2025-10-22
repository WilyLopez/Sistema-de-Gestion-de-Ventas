package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.DetallePedidoReabastecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para la entidad {@link DetallePedidoReabastecimiento}.
 * Proporciona métodos para gestionar los detalles asociados a pedidos de reabastecimiento
 * (RF-011 y RF-014).
 *
 * <p>Permite realizar consultas por pedido, producto, estado y obtener métricas
 * de cantidades solicitadas y recibidas.</p>
 *
 * @author Wilian Lopez
 * @version 1.1
 * @since 2024
 */
@Repository
public interface DetallePedidoReabastecimientoRepository extends JpaRepository<DetallePedidoReabastecimiento, Long> {

    /**
     * Obtiene todos los detalles asociados a un pedido de reabastecimiento específico.
     *
     * @param idPedidoReab ID del pedido de reabastecimiento
     * @return Lista de detalles correspondientes al pedido
     */
    List<DetallePedidoReabastecimiento> findByPedidoReabastecimiento_IdPedidoReab(Long idPedidoReab);

    /**
     * Obtiene todos los detalles asociados a un producto específico.
     *
     * @param idProducto ID del producto
     * @return Lista de detalles que contienen el producto especificado
     */
    List<DetallePedidoReabastecimiento> findByProductoIdProducto(Long idProducto);

    /**
     * Obtiene los detalles de pedidos que aún no han sido completamente recibidos.
     *
     * @return Lista de detalles pendientes o parcialmente recibidos
     */
    @Query("SELECT dpr FROM DetallePedidoReabastecimiento dpr WHERE dpr.estado IN ('PENDIENTE', 'RECIBIDO_PARCIAL')")
    List<DetallePedidoReabastecimiento> findDetallesPendientesRecepcion();

    /**
     * Calcula la cantidad total solicitada de un producto en todos los pedidos.
     *
     * @param idProducto ID del producto
     * @return Cantidad total solicitada (0 si no existen registros)
     */
    @Query("SELECT COALESCE(SUM(dpr.cantidadSolicitada), 0) FROM DetallePedidoReabastecimiento dpr WHERE dpr.producto.id = :idProducto")
    Integer getCantidadTotalSolicitada(@Param("idProducto") Long idProducto);

    /**
     * Calcula la cantidad total recibida de un producto en todos los pedidos.
     *
     * @param idProducto ID del producto
     * @return Cantidad total recibida (0 si no existen registros)
     */
    @Query("SELECT COALESCE(SUM(dpr.cantidadRecibida), 0) FROM DetallePedidoReabastecimiento dpr WHERE dpr.producto.id = :idProducto")
    Integer getCantidadTotalRecibida(@Param("idProducto") Long idProducto);
}
