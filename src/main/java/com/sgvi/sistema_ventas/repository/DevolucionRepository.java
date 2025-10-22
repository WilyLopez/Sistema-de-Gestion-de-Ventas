package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.Devolucion;
import com.sgvi.sistema_ventas.model.enums.EstadoDevolucion;
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
 * Repository para la entidad Devolucion.
 * Proporciona métodos para gestionar devoluciones según RF-013
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {

    // RF-013: Devoluciones por venta
    List<Devolucion> findByVentaIdVenta(Long idVenta);

    List<Devolucion> findByFechaDevolucionBetween(LocalDateTime inicio, LocalDateTime fin);
    // RF-013: Devoluciones por cliente
    @Query("SELECT d FROM Devolucion d WHERE d.venta.cliente.idCliente = :idCliente")
    Page<Devolucion> findByClienteId(@Param("idCliente") Long idCliente, Pageable pageable);

    // RF-013: Devoluciones por usuario
    Page<Devolucion> findByUsuarioIdUsuario(Long idUsuario, Pageable pageable);

    // RF-013: Devoluciones por estado
    Page<Devolucion> findByEstado(EstadoDevolucion estado, Pageable pageable);

    // RF-013: Devoluciones por rango de fechas
    Page<Devolucion> findByFechaDevolucionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);

    // RF-013: Búsqueda combinada con filtros
    @Query("SELECT d FROM Devolucion d WHERE " +
            "(:idVenta IS NULL OR d.venta.idVenta = :idVenta) AND " +          // ⬅️ CAMBIO: d.venta.idVenta
            "(:idCliente IS NULL OR d.venta.cliente.idCliente = :idCliente) AND " + // ⬅️ CAMBIO: d.venta.cliente.idCliente
            "(:idUsuario IS NULL OR d.usuario.idUsuario = :idUsuario) AND " +    // ⬅️ CAMBIO: d.usuario.idUsuario
            "(:estado IS NULL OR d.estado = :estado) AND " +
            "(:fechaInicio IS NULL OR d.fechaDevolucion >= :fechaInicio) AND " +
            "(:fechaFin IS NULL OR d.fechaDevolucion <= :fechaFin)")
    Page<Devolucion> buscarDevolucionesConFiltros(
            @Param("idVenta") Long idVenta,
            @Param("idCliente") Long idCliente,
            @Param("idUsuario") Long idUsuario,
            @Param("estado") EstadoDevolucion estado,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);

    // RF-013: Verificar si venta tiene devoluciones aprobadas o completadas
    @Query("SELECT COUNT(d) > 0 FROM Devolucion d WHERE d.venta.id = :idVenta AND d.estado IN ('APROBADA', 'COMPLETADA')")
    boolean existsDevolucionesAprobadasPorVenta(@Param("idVenta") Long idVenta);

    // RF-013: Devoluciones dentro del plazo de 30 días
    @Query("SELECT d FROM Devolucion d WHERE d.venta.id = :idVenta AND d.fechaDevolucion <= :fechaLimite")
    List<Devolucion> findDevolucionesDentroPlazo(@Param("idVenta") Long idVenta,
                                                 @Param("fechaLimite") LocalDateTime fechaLimite);

    // RF-014: Reporte de devoluciones por período
    @Query("SELECT d FROM Devolucion d WHERE d.fechaDevolucion BETWEEN :fechaInicio AND :fechaFin ORDER BY d.fechaDevolucion DESC")
    List<Devolucion> findDevolucionesPorPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio,
                                                @Param("fechaFin") LocalDateTime fechaFin);

    // RF-014: Análisis de motivos de devolución
    @Query("SELECT d.motivo, COUNT(d) FROM Devolucion d WHERE d.fechaDevolucion BETWEEN :fechaInicio AND :fechaFin GROUP BY d.motivo")
    List<Object[]> getDevolucionesPorMotivo(@Param("fechaInicio") LocalDateTime fechaInicio,
                                            @Param("fechaFin") LocalDateTime fechaFin);

    // RF-014: Productos más devueltos
    @Query("SELECT dd.producto.nombre, COUNT(dd) as vecesDevuelto " +
            "FROM DetalleDevolucion dd " +
            "WHERE dd.devolucion.fechaDevolucion BETWEEN :fechaInicio AND :fechaFin " +
            "AND dd.devolucion.estado IN ('APROBADA', 'COMPLETADA') " +
            "GROUP BY dd.producto.id, dd.producto.nombre " +
            "ORDER BY vecesDevuelto DESC")
    List<Object[]> getProductosMasDevueltos(@Param("fechaInicio") LocalDateTime fechaInicio,
                                            @Param("fechaFin") LocalDateTime fechaFin,
                                            Pageable pageable);

    // Verificar si existe devolución para un producto en una venta
    @Query("SELECT COUNT(dd) > 0 FROM DetalleDevolucion dd WHERE dd.devolucion.venta.id = :idVenta AND dd.producto.id = :idProducto")
    boolean existsDevolucionParaProductoEnVenta(@Param("idVenta") Long idVenta,
                                                @Param("idProducto") Long idProducto);
}