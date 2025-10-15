package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.DetalleDevolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad DetalleDevolucion.
 * Proporciona métodos para gestionar detalles de devolución
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Repository
public interface DetalleDevolucionRepository extends JpaRepository<DetalleDevolucion, Long> {

    // Buscar detalles por devolución
    List<DetalleDevolucion> findByDevolucionId(Long idDevolucion);

    // Buscar detalles por producto
    List<DetalleDevolucion> findByProductoId(Long idProducto);

    // RF-013: Cantidad total devuelta de un producto
    @Query("SELECT COALESCE(SUM(dd.cantidad), 0) FROM DetalleDevolucion dd WHERE dd.producto.id = :idProducto AND dd.devolucion.estado IN ('APROBADA', 'COMPLETADA')")
    Integer getCantidadTotalDevuelta(@Param("idProducto") Long idProducto);

    // RF-013: Verificar cantidad máxima devolvable
    @Query("SELECT dv.cantidad FROM DetalleVenta dv WHERE dv.venta.id = :idVenta AND dv.producto.id = :idProducto")
    Optional<Integer> getCantidadVendida(@Param("idVenta") Long idVenta,
                                         @Param("idProducto") Long idProducto);

    // RF-013: Cantidad ya devuelta de un producto en una venta
    @Query("SELECT COALESCE(SUM(dd.cantidad), 0) FROM DetalleDevolucion dd WHERE dd.devolucion.venta.id = :idVenta AND dd.producto.id = :idProducto AND dd.devolucion.estado IN ('APROBADA', 'COMPLETADA')")
    Integer getCantidadYaDevuelta(@Param("idVenta") Long idVenta,
                                  @Param("idProducto") Long idProducto);
}