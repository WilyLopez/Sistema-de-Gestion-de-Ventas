package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.Pedido;
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
 * Repository para la entidad Pedido.
 * Proporciona métodos para gestionar pedidos de clientes
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Buscar por código único
    Optional<Pedido> findByCodigoPedido(String codigoPedido);

    // Pedidos por cliente
    Page<Pedido> findByClienteId(Long idCliente, Pageable pageable);

    // Pedidos por usuario
    Page<Pedido> findByUsuarioId(Long idUsuario, Pageable pageable);

    // Pedidos por estado
    Page<Pedido> findByEstado(EstadoPedido estado, Pageable pageable);

    // Pedidos por rango de fechas
    Page<Pedido> findByFechaPedidoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);

    // Búsqueda combinada con filtros
    @Query("SELECT p FROM Pedido p WHERE " +
            "(:codigoPedido IS NULL OR p.codigoPedido LIKE %:codigoPedido%) AND " +
            "(:idCliente IS NULL OR p.cliente.id = :idCliente) AND " +
            "(:idUsuario IS NULL OR p.usuario.id = :idUsuario) AND " +
            "(:estado IS NULL OR p.estado = :estado) AND " +
            "(:fechaInicio IS NULL OR p.fechaPedido >= :fechaInicio) AND " +
            "(:fechaFin IS NULL OR p.fechaPedido <= :fechaFin)")
    Page<Pedido> buscarPedidosConFiltros(
            @Param("codigoPedido") String codigoPedido,
            @Param("idCliente") Long idCliente,
            @Param("idUsuario") Long idUsuario,
            @Param("estado") EstadoPedido estado,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);

    // Pedidos pendientes de entrega
    @Query("SELECT p FROM Pedido p WHERE p.estado IN ('CONFIRMADO', 'PREPARANDO', 'ENVIADO') AND p.fechaEntrega <= :fechaLimite")
    List<Pedido> findPedidosPendientesEntrega(@Param("fechaLimite") LocalDateTime fechaLimite);

    // Pedidos atrasados
    @Query("SELECT p FROM Pedido p WHERE p.estado IN ('CONFIRMADO', 'PREPARANDO', 'ENVIADO') AND p.fechaEntrega < :fechaActual")
    List<Pedido> findPedidosAtrasados(@Param("fechaActual") LocalDateTime fechaActual);

    // RF-014: Reporte de pedidos por período
    @Query("SELECT p FROM Pedido p WHERE p.fechaPedido BETWEEN :fechaInicio AND :fechaFin ORDER BY p.fechaPedido DESC")
    List<Pedido> findPedidosPorPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio,
                                       @Param("fechaFin") LocalDateTime fechaFin);

    // RF-014: Estadísticas de pedidos
    @Query("SELECT p.estado, COUNT(p) FROM Pedido p WHERE p.fechaPedido BETWEEN :fechaInicio AND :fechaFin GROUP BY p.estado")
    List<Object[]> getEstadisticasPedidos(@Param("fechaInicio") LocalDateTime fechaInicio,
                                          @Param("fechaFin") LocalDateTime fechaFin);

    // Contar pedidos por estado
    long countByEstado(EstadoPedido estado);
}