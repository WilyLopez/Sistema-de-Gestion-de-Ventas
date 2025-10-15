package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para la entidad DetalleVenta.
 * Proporciona métodos para gestionar detalles de venta
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    // Buscar detalles por venta
    List<DetalleVenta> findByVentaId(Long idVenta);

    // Buscar detalles por producto
    List<DetalleVenta> findByProductoId(Long idProducto);

    // RF-008: Ver productos vendidos en un período
    @Query("SELECT dv FROM DetalleVenta dv WHERE dv.venta.fechaVenta BETWEEN :fechaInicio AND :fechaFin")
    List<DetalleVenta> findDetallesPorPeriodo(@Param("fechaInicio") java.time.LocalDateTime fechaInicio,
                                              @Param("fechaFin") java.time.LocalDateTime fechaFin);

    // RF-014: Cantidad total vendida de un producto
    @Query("SELECT COALESCE(SUM(dv.cantidad), 0) FROM DetalleVenta dv WHERE dv.producto.id = :idProducto AND dv.venta.estado = 'PAGADO'")
    Integer getCantidadTotalVendida(@Param("idProducto") Long idProducto);

    // RF-014: Productos vendidos por un cliente
    @Query("SELECT dv FROM DetalleVenta dv WHERE dv.venta.cliente.id = :idCliente")
    List<DetalleVenta> findDetallesPorCliente(@Param("idCliente") Long idCliente);
}