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
 * Proporciona métodos para gestionar pedidos de clientes, incluyendo búsquedas por
 * código, cliente, usuario, estado y rango de fechas, así como reportes específicos
 * para estadísticas y entregas.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * Busca un pedido por su código único.
     * @param codigoPedido El código único del pedido.
     * @return Un Optional que contiene el Pedido si existe.
     */
    Optional<Pedido> findByCodigoPedido(String codigoPedido);

    /**
     * Obtiene una página de pedidos asociados a un cliente específico.
     * @param idCliente El ID de la entidad Cliente.
     * @param pageable La información de paginación.
     * @return Una página de Pedidos.
     */
    Page<Pedido> findByClienteIdCliente(Long idCliente, Pageable pageable); // ⬅️ CORREGIDO

    /**
     * Obtiene una página de pedidos registrados por un usuario específico.
     * @param idUsuario El ID de la entidad Usuario (vendedor/registrador).
     * @param pageable La información de paginación.
     * @return Una página de Pedidos.
     */
    Page<Pedido> findByUsuarioIdUsuario(Long idUsuario, Pageable pageable); // ⬅️ CORREGIDO

    /**
     * Obtiene una página de pedidos filtrados por estado.
     * @param estado El estado del pedido (e.g., PENDIENTE, ENVIADO).
     * @param pageable La información de paginación.
     * @return Una página de Pedidos.
     */
    Page<Pedido> findByEstado(EstadoPedido estado, Pageable pageable);

    /**
     * Obtiene una página de pedidos filtrados por el rango de fecha de creación.
     * @param fechaInicio Fecha y hora de inicio del rango.
     * @param fechaFin Fecha y hora de fin del rango.
     * @param pageable La información de paginación.
     * @return Una página de Pedidos.
     */
    Page<Pedido> findByFechaPedidoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);

    /**
     * Realiza una búsqueda combinada de pedidos aplicando múltiples filtros opcionales.
     * @param codigoPedido Código del pedido o parte de él.
     * @param idCliente ID del cliente.
     * @param idUsuario ID del usuario que registró el pedido.
     * @param estado Estado del pedido.
     * @param fechaInicio Fecha de inicio para el rango de la fecha del pedido.
     * @param fechaFin Fecha de fin para el rango de la fecha del pedido.
     * @param pageable La información de paginación.
     * @return Una página de Pedidos que cumplen con los criterios.
     */
    @Query("SELECT p FROM Pedido p WHERE " +
            "(:codigoPedido IS NULL OR p.codigoPedido LIKE %:codigoPedido%) AND " +
            "(:idCliente IS NULL OR p.cliente.idCliente = :idCliente) AND " + // ⬅️ CORREGIDO
            "(:idUsuario IS NULL OR p.usuario.idUsuario = :idUsuario) AND " + // ⬅️ CORREGIDO
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

    /**
     * Obtiene pedidos que están en estados de proceso (CONFIRMADO, PREPARANDO, ENVIADO)
     * cuya fecha de entrega es anterior o igual a un límite dado.
     * @param fechaLimite Fecha límite para la entrega.
     * @return Lista de Pedidos pendientes de entrega.
     */
    @Query("SELECT p FROM Pedido p WHERE p.estado IN ('CONFIRMADO', 'PREPARANDO', 'ENVIADO') AND p.fechaEntrega <= :fechaLimite")
    List<Pedido> findPedidosPendientesEntrega(@Param("fechaLimite") LocalDateTime fechaLimite);

    /**
     * Obtiene pedidos que están en estados de proceso (CONFIRMADO, PREPARANDO, ENVIADO)
     * y cuya fecha de entrega ha pasado la fecha actual.
     * @param fechaActual La fecha y hora actual para la comparación.
     * @return Lista de Pedidos atrasados.
     */
    @Query("SELECT p FROM Pedido p WHERE p.estado IN ('CONFIRMADO', 'PREPARANDO', 'ENVIADO') AND p.fechaEntrega < :fechaActual")
    List<Pedido> findPedidosAtrasados(@Param("fechaActual") LocalDateTime fechaActual);

    /**
     * (RF-014) Obtiene todos los pedidos dentro de un rango de fecha de creación.
     * @param fechaInicio Fecha de inicio del reporte.
     * @param fechaFin Fecha de fin del reporte.
     * @return Lista de Pedidos ordenados por fecha de creación descendente.
     */
    @Query("SELECT p FROM Pedido p WHERE p.fechaPedido BETWEEN :fechaInicio AND :fechaFin ORDER BY p.fechaPedido DESC")
    List<Pedido> findPedidosPorPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio,
                                       @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * (RF-014) Obtiene el conteo de pedidos agrupados por estado para un período dado.
     * @param fechaInicio Fecha de inicio del reporte.
     * @param fechaFin Fecha de fin del reporte.
     * @return Lista de arreglos Object[] con el estado y la cantidad (COUNT).
     */
    @Query("SELECT p.estado, COUNT(p) FROM Pedido p WHERE p.fechaPedido BETWEEN :fechaInicio AND :fechaFin GROUP BY p.estado")
    List<Object[]> getEstadisticasPedidos(@Param("fechaInicio") LocalDateTime fechaInicio,
                                          @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Cuenta el número total de pedidos que se encuentran en un estado específico.
     * @param estado El estado del pedido.
     * @return El número de pedidos en ese estado.
     */
    long countByEstado(EstadoPedido estado);
}