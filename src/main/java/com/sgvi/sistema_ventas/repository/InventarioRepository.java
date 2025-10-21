package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.Inventario;
import com.sgvi.sistema_ventas.model.enums.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad Inventario.
 * Proporciona métodos para gestionar movimientos de inventario según RF-012
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    // RF-012: Movimientos por producto
    Page<Inventario> findByProductoId(Long idProducto, Pageable pageable);

    // RF-012: Movimientos por tipo
    Page<Inventario> findByTipoMovimiento(TipoMovimiento tipoMovimiento, Pageable pageable);

    List<Inventario> findByFechaMovimientoBetween(LocalDateTime inicio, LocalDateTime fin);

    // RF-012: Movimientos por usuario
    Page<Inventario> findByUsuarioId(Long idUsuario, Pageable pageable);

    // RF-012: Movimientos por rango de fechas
    Page<Inventario> findByFechaMovimientoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);

    // RF-012: Movimientos por venta
    List<Inventario> findByVentaId(Long idVenta);

    // RF-012: Búsqueda combinada con filtros
    @Query("SELECT i FROM Inventario i WHERE " +
            "(:idProducto IS NULL OR i.producto.id = :idProducto) AND " +
            "(:tipoMovimiento IS NULL OR i.tipoMovimiento = :tipoMovimiento) AND " +
            "(:idUsuario IS NULL OR i.usuario.id = :idUsuario) AND " +
            "(:fechaInicio IS NULL OR i.fechaMovimiento >= :fechaInicio) AND " +
            "(:fechaFin IS NULL OR i.fechaMovimiento <= :fechaFin)")
    Page<Inventario> buscarMovimientosConFiltros(
            @Param("idProducto") Long idProducto,
            @Param("tipoMovimiento") TipoMovimiento tipoMovimiento,
            @Param("idUsuario") Long idUsuario,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);

    // RF-012: Trazabilidad completa de un producto
    @Query("SELECT i FROM Inventario i WHERE i.producto.id = :idProducto ORDER BY i.fechaMovimiento DESC")
    List<Inventario> findTrazabilidadProducto(@Param("idProducto") Long idProducto);

    // RF-014: Reporte de movimientos por período
    @Query("SELECT i FROM Inventario i WHERE i.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin ORDER BY i.fechaMovimiento DESC")
    List<Inventario> findMovimientosPorPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio,
                                               @Param("fechaFin") LocalDateTime fechaFin);

    // RF-014: Total de entradas por producto en período
    @Query("SELECT COALESCE(SUM(i.cantidad), 0) FROM Inventario i WHERE i.producto.id = :idProducto " +
            "AND i.tipoMovimiento IN ('ENTRADA', 'DEVOLUCION') AND i.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin")
    Integer getTotalEntradasProducto(@Param("idProducto") Long idProducto,
                                     @Param("fechaInicio") LocalDateTime fechaInicio,
                                     @Param("fechaFin") LocalDateTime fechaFin);

    // RF-014: Total de salidas por producto en período
    @Query("SELECT COALESCE(SUM(i.cantidad), 0) FROM Inventario i WHERE i.producto.id = :idProducto " +
            "AND i.tipoMovimiento = 'SALIDA' AND i.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin")
    Integer getTotalSalidasProducto(@Param("idProducto") Long idProducto,
                                    @Param("fechaInicio") LocalDateTime fechaInicio,
                                    @Param("fechaFin") LocalDateTime fechaFin);

    // Último movimiento de un producto
    Optional<Inventario> findFirstByProductoIdOrderByFechaMovimientoDesc(Long idProducto);
}