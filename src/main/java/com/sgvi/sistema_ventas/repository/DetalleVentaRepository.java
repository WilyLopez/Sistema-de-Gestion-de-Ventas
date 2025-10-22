package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para la entidad DetalleVenta.
 * Proporciona métodos para gestionar detalles de venta, incluyendo búsquedas por
 * venta, producto y reportes de ventas.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    /**
     * Busca todos los detalles de venta asociados a un ID de Venta específico.
     * @param idVenta El ID de la Venta.
     * @return Una lista de DetalleVenta que pertenecen a la Venta.
     */
    List<DetalleVenta> findByVentaIdVenta(Long idVenta);

    /**
     * Busca todos los detalles de venta que contienen un ID de Producto específico.
     * @param idProducto El ID del Producto.
     * @return Una lista de DetalleVenta que contienen el Producto.
     */
    List<DetalleVenta> findByProductoIdProducto(Long idProducto);

    // RF-008: Ver productos vendidos en un período
    @Query("SELECT dv FROM DetalleVenta dv WHERE dv.venta.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
    List<DetalleVenta> findDetallesPorPeriodo(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );


    /**
     * (RF-014) Obtiene la cantidad total vendida de un producto cuyo estado de venta es 'PAGADO'.
     * @param idProducto El ID del Producto.
     * @return La cantidad total vendida.
     */
    @Query("SELECT COALESCE(SUM(dv.cantidad), 0) FROM DetalleVenta dv WHERE dv.producto.idProducto = :idProducto AND dv.venta.estado = 'PAGADO'") // ⬅️ CORREGIDO
    Integer getCantidadTotalVendida(@Param("idProducto") Long idProducto);

    /**
     * (RF-014) Obtiene todos los detalles de venta asociados a un cliente específico.
     * @param idCliente El ID del Cliente.
     * @return Una lista de DetalleVenta asociados al cliente.
     */
    @Query("SELECT dv FROM DetalleVenta dv WHERE dv.venta.cliente.idCliente = :idCliente")
    List<DetalleVenta> findDetallesPorCliente(@Param("idCliente") Long idCliente);
}