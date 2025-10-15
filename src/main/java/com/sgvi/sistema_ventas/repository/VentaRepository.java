package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.Venta;
import com.sgvi.sistema_ventas.model.enums.EstadoVenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad Venta.
 * Proporciona métodos para gestionar ventas según RF-007, RF-008, RF-009
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // RF-008: Consulta de ventas con filtros
    Optional<Venta> findByCodigoVenta(String codigoVenta);

    // RF-008: Búsqueda por cliente
    Page<Venta> findByClienteId(Long idCliente, Pageable pageable);

    // RF-008: Búsqueda por vendedor
    Page<Venta> findByUsuarioId(Long idUsuario, Pageable pageable);

    // RF-008: Filtro por estado
    Page<Venta> findByEstado(EstadoVenta estado, Pageable pageable);

    // RF-008: Filtro por método de pago
    Page<Venta> findByMetodoPagoId(Long idMetodoPago, Pageable pageable);

    // RF-008: Búsqueda por rango de fechas
    Page<Venta> findByFechaVentaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);

    // RF-008: Búsqueda combinada múltiples filtros
    @Query("SELECT v FROM Venta v WHERE " +
            "(:codigoVenta IS NULL OR v.codigoVenta LIKE %:codigoVenta%) AND " +
            "(:idCliente IS NULL OR v.cliente.id = :idCliente) AND " +
            "(:idUsuario IS NULL OR v.usuario.id = :idUsuario) AND " +
            "(:estado IS NULL OR v.estado = :estado) AND " +
            "(:idMetodoPago IS NULL OR v.metodoPago.id = :idMetodoPago) AND " +
            "(:fechaInicio IS NULL OR v.fechaVenta >= :fechaInicio) AND " +
            "(:fechaFin IS NULL OR v.fechaVenta <= :fechaFin)")
    Page<Venta> buscarVentasConFiltros(
            @Param("codigoVenta") String codigoVenta,
            @Param("idCliente") Long idCliente,
            @Param("idUsuario") Long idUsuario,
            @Param("estado") EstadoVenta estado,
            @Param("idMetodoPago") Long idMetodoPago,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);

    // RF-009: Verificar si venta puede ser anulada (menos de 24 horas)
    @Query("SELECT v FROM Venta v WHERE v.id = :idVenta AND v.estado = 'PAGADO' AND v.fechaVenta >= :fechaLimite")
    Optional<Venta> findVentaAnulable(@Param("idVenta") Long idVenta, @Param("fechaLimite") LocalDateTime fechaLimite);

    // RF-014: Reporte de ventas por período
    @Query("SELECT v FROM Venta v WHERE v.fechaVenta BETWEEN :fechaInicio AND :fechaFin ORDER BY v.fechaVenta DESC")
    List<Venta> findVentasPorPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio,
                                     @Param("fechaFin") LocalDateTime fechaFin);

    // RF-014: Total de ventas por período
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fechaVenta BETWEEN :fechaInicio AND :fechaFin AND v.estado = 'PAGADO'")
    BigDecimal getTotalVentasPorPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio,
                                        @Param("fechaFin") LocalDateTime fechaFin);

    // RF-014: Cantidad de ventas por período
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.fechaVenta BETWEEN :fechaInicio AND :fechaFin AND v.estado = 'PAGADO'")
    Long countVentasPorPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio,
                               @Param("fechaFin") LocalDateTime fechaFin);

    // RF-014: Ventas por categoría
    @Query("SELECT dv.producto.categoria.nombre, SUM(dv.subtotal) " +
            "FROM DetalleVenta dv " +
            "WHERE dv.venta.fechaVenta BETWEEN :fechaInicio AND :fechaFin " +
            "AND dv.venta.estado = 'PAGADO' " +
            "GROUP BY dv.producto.categoria.nombre")
    List<Object[]> getVentasPorCategoria(@Param("fechaInicio") LocalDateTime fechaInicio,
                                         @Param("fechaFin") LocalDateTime fechaFin);

    // RF-014: Top productos más vendidos
    @Query("SELECT dv.producto.nombre, SUM(dv.cantidad) as totalVendido " +
            "FROM DetalleVenta dv " +
            "WHERE dv.venta.fechaVenta BETWEEN :fechaInicio AND :fechaFin " +
            "AND dv.venta.estado = 'PAGADO' " +
            "GROUP BY dv.producto.id, dv.producto.nombre " +
            "ORDER BY totalVendido DESC")
    List<Object[]> getTopProductosVendidos(@Param("fechaInicio") LocalDateTime fechaInicio,
                                           @Param("fechaFin") LocalDateTime fechaFin,
                                           Pageable pageable);

    // Verificar existencia de ventas para un cliente
    boolean existsByClienteId(Long idCliente);

    // Verificar existencia de ventas para un usuario
    boolean existsByUsuarioId(Long idUsuario);
}