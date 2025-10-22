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

    // --- Métodos de Búsqueda por Nombre ---

    // Buscar detalles por devolución
    List<DetalleDevolucion> findByDevolucionIdDevolucion(Long idDevolucion);

    // Buscar detalles por producto
    List<DetalleDevolucion> findByProductoIdProducto(Long idProducto);


    // RF-013: Cantidad total devuelta de un producto
    @Query("SELECT COALESCE(SUM(dd.cantidad), 0) FROM DetalleDevolucion dd WHERE dd.producto.idProducto = :idProducto AND dd.devolucion.estado IN ('APROBADA', 'COMPLETADA')")
    Integer getCantidadTotalDevuelta(@Param("idProducto") Long idProducto);

    // RF-013: Verificar cantidad máxima devolvable
    @Query("SELECT dv.cantidad FROM DetalleVenta dv WHERE dv.venta.idVenta = :idVenta AND dv.producto.idProducto = :idProducto")
    Optional<Integer> getCantidadVendida(@Param("idVenta") Long idVenta,
                                         @Param("idProducto") Long idProducto);

    // RF-013: Cantidad ya devuelta de un producto en una venta
    @Query("SELECT COALESCE(SUM(dd.cantidad), 0) FROM DetalleDevolucion dd WHERE dd.devolucion.venta.idVenta = :idVenta AND dd.producto.idProducto = :idProducto AND dd.devolucion.estado IN ('APROBADA', 'COMPLETADA')")
    Integer getCantidadYaDevuelta(@Param("idVenta") Long idVenta,
                                  @Param("idProducto") Long idProducto);
}