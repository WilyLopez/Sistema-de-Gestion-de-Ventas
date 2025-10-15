package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.PedidoReabastecimiento;
import com.sgvi.sistema_ventas.model.enums.EstadoPedido;
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
 * Repository para la entidad PedidoReabastecimiento.
 * Proporciona métodos para gestionar pedidos de reabastecimiento según RF-011
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Repository
public interface PedidoReabastecimientoRepository extends JpaRepository<PedidoReabastecimiento, Long> {

    // RF-011: Buscar por código único
    Optional<PedidoReabastecimiento> findByCodigoPedido(String codigoPedido);

    // RF-011: Pedidos por proveedor
    Page<PedidoReabastecimiento> findByProveedorId(Long idProveedor, Pageable pageable);

    // RF-011: Pedidos por usuario solicitante
    Page<PedidoReabastecimiento> findByUsuarioSolicitanteId(Long idUsuario, Pageable pageable);

    // RF-011: Pedidos por estado
    Page<PedidoReabastecimiento> findByEstado(EstadoPedido estado, Pageable pageable);

    // RF-011: Pedidos por prioridad
    Page<PedidoReabastecimiento> findByPrioridad(String prioridad, Pageable pageable);

    // RF-011: Pedidos por rango de fechas
    Page<PedidoReabastecimiento> findByFechaSolicitudBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);

    // RF-011: Búsqueda combinada con filtros
    @Query("SELECT pr FROM PedidoReabastecimiento pr WHERE " +
            "(:codigoPedido IS NULL OR pr.codigoPedido LIKE %:codigoPedido%) AND " +
            "(:idProveedor IS NULL OR pr.proveedor.id = :idProveedor) AND " +
            "(:idUsuario IS NULL OR pr.usuarioSolicitante.id = :idUsuario) AND " +
            "(:estado IS NULL OR pr.estado = :estado) AND " +
            "(:prioridad IS NULL OR pr.prioridad = :prioridad) AND " +
            "(:fechaInicio IS NULL OR pr.fechaSolicitud >= :fechaInicio) AND " +
            "(:fechaFin IS NULL OR pr.fechaSolicitud <= :fechaFin)")
    Page<PedidoReabastecimiento> buscarPedidosReabastecimientoConFiltros(
            @Param("codigoPedido") String codigoPedido,
            @Param("idProveedor") Long idProveedor,
            @Param("idUsuario") Long idUsuario,
            @Param("estado") EstadoPedido estado,
            @Param("prioridad") String prioridad,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);

    // RF-011: Pedidos pendientes de recepción
    @Query("SELECT pr FROM PedidoReabastecimiento pr WHERE pr.estado IN ('APROBADO', 'ORDENADO', 'RECIBIDO_PARCIAL')")
    List<PedidoReabastecimiento> findPedidosPendientesRecepcion();

    // RF-011: Pedidos urgentes
    @Query("SELECT pr FROM PedidoReabastecimiento pr WHERE pr.prioridad = 'urgente' AND pr.estado IN ('PENDIENTE', 'APROBADO')")
    List<PedidoReabastecimiento> findPedidosUrgentes();

    // RF-014: Reporte de reabastecimientos por período
    @Query("SELECT pr FROM PedidoReabastecimiento pr WHERE pr.fechaSolicitud BETWEEN :fechaInicio AND :fechaFin ORDER BY pr.fechaSolicitud DESC")
    List<PedidoReabastecimiento> findReabastecimientosPorPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio,
                                                                 @Param("fechaFin") LocalDateTime fechaFin);

    // Contar pedidos por estado
    long countByEstado(EstadoPedido estado);
}