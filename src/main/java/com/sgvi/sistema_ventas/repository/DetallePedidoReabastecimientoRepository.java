package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.DetallePedidoReabastecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para la entidad DetallePedidoReabastecimiento.
 * Proporciona métodos para gestionar detalles de reabastecimiento
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Repository
public interface DetallePedidoReabastecimientoRepository extends JpaRepository<DetallePedidoReabastecimiento, Long> {

    // Buscar detalles por pedido de reabastecimiento
    List<DetallePedidoReabastecimiento> findByPedidoReabastecimientoId(Long idPedidoReabastecimiento);

    // Buscar detalles por producto
    List<DetallePedidoReabastecimiento> findByProductoId(Long idProducto);

    // RF-011: Productos pendientes de reabastecimiento
    @Query("SELECT dpr FROM DetallePedidoReabastecimiento dpr WHERE dpr.estado IN ('PENDIENTE', 'RECIBIDO_PARCIAL')")
    List<DetallePedidoReabastecimiento> findDetallesPendientesRecepcion();

    // RF-011: Cantidad total solicitada de un producto
    @Query("SELECT COALESCE(SUM(dpr.cantidadSolicitada), 0) FROM DetallePedidoReabastecimiento dpr WHERE dpr.producto.id = :idProducto")
    Integer getCantidadTotalSolicitada(@Param("idProducto") Long idProducto);

    // RF-011: Cantidad total recibida de un producto
    @Query("SELECT COALESCE(SUM(dpr.cantidadRecibida), 0) FROM DetallePedidoReabastecimiento dpr WHERE dpr.producto.id = :idProducto")
    Integer getCantidadTotalRecibida(@Param("idProducto") Long idProducto);
}