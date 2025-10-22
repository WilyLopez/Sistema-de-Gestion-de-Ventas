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
 * Repository para la entidad {@link PedidoReabastecimiento}.
 * <p>
 * Proporciona métodos especializados para gestionar los pedidos de reabastecimiento
 * según los requerimientos funcionales (RF-011, RF-014) del sistema de ventas.
 * <p>
 * Implementa consultas automáticas basadas en convenciones de Spring Data JPA
 * y queries personalizadas para búsquedas filtradas y reportes.
 *
 * @author
 *     Wilian Lopez
 * @version
 *     1.1
 * @since
 *     2024
 */
@Repository
public interface PedidoReabastecimientoRepository extends JpaRepository<PedidoReabastecimiento, Long> {

    /**
     * Busca un pedido por su código único.
     *
     * @param codigoPedido código único del pedido (ejemplo: "REAB-2024-00001")
     * @return pedido encontrado o vacío si no existe
     */
    Optional<PedidoReabastecimiento> findByCodigoPedido(String codigoPedido);

    /**
     * Obtiene los pedidos asociados a un proveedor específico.
     *
     * @param idProveedor ID del proveedor
     * @param pageable    paginación
     * @return página con los pedidos del proveedor
     */
    Page<PedidoReabastecimiento> findByProveedor_IdProveedor(Long idProveedor, Pageable pageable);

    /**
     * Obtiene los pedidos solicitados por un usuario específico.
     * <p>
     * ⚠️ Usa la propiedad {@code idUsuario} de la entidad {@code Usuario}
     * para evitar el error de mapeo.
     *
     * @param idUsuario ID del usuario solicitante
     * @param pageable  paginación
     * @return página con los pedidos del usuario solicitante
     */
    Page<PedidoReabastecimiento> findByUsuarioSolicitanteIdUsuario(Long idUsuario, Pageable pageable);

    /**
     * Lista los pedidos por su estado actual.
     *
     * @param estado   estado del pedido
     * @param pageable paginación
     * @return página de pedidos filtrados por estado
     */
    Page<PedidoReabastecimiento> findByEstado(EstadoPedido estado, Pageable pageable);

    /**
     * Lista los pedidos según su nivel de prioridad.
     *
     * @param prioridad valor de prioridad (ejemplo: "normal", "urgente")
     * @param pageable  paginación
     * @return página con los pedidos de esa prioridad
     */
    Page<PedidoReabastecimiento> findByPrioridad(String prioridad, Pageable pageable);

    /**
     * Obtiene los pedidos realizados en un rango de fechas determinado.
     *
     * @param fechaInicio fecha inicial (inclusive)
     * @param fechaFin    fecha final (inclusive)
     * @param pageable    paginación
     * @return página con los pedidos del rango indicado
     */
    Page<PedidoReabastecimiento> findByFechaSolicitudBetween(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable
    );

    /**
     * Búsqueda avanzada de pedidos de reabastecimiento con múltiples filtros opcionales.
     * <p>
     * Todos los parámetros son opcionales (pueden ser null).
     *
     * @param codigoPedido filtro por código (opcional)
     * @param idProveedor  filtro por proveedor (opcional)
     * @param idUsuario    filtro por usuario solicitante (opcional)
     * @param estado       filtro por estado (opcional)
     * @param prioridad    filtro por prioridad (opcional)
     * @param fechaInicio  filtro por fecha inicial (opcional)
     * @param fechaFin     filtro por fecha final (opcional)
     * @param pageable     configuración de paginación
     * @return página con resultados que cumplen los filtros
     */
    @Query("""
            SELECT pr FROM PedidoReabastecimiento pr 
            WHERE (:codigoPedido IS NULL OR pr.codigoPedido LIKE %:codigoPedido%) 
              AND (:idProveedor IS NULL OR pr.proveedor.id = :idProveedor)
              AND (:idUsuario IS NULL OR pr.usuarioSolicitante.idUsuario = :idUsuario)
              AND (:estado IS NULL OR pr.estado = :estado)
              AND (:prioridad IS NULL OR pr.prioridad = :prioridad)
              AND (:fechaInicio IS NULL OR pr.fechaSolicitud >= :fechaInicio)
              AND (:fechaFin IS NULL OR pr.fechaSolicitud <= :fechaFin)
            """)
    Page<PedidoReabastecimiento> buscarPedidosReabastecimientoConFiltros(
            @Param("codigoPedido") String codigoPedido,
            @Param("idProveedor") Long idProveedor,
            @Param("idUsuario") Long idUsuario,
            @Param("estado") EstadoPedido estado,
            @Param("prioridad") String prioridad,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable
    );

    /**
     * Obtiene los pedidos que aún están pendientes de recepción.
     * <p>
     * Incluye estados: APROBADO, ORDENADO, RECIBIDO_PARCIAL.
     *
     * @return lista de pedidos pendientes de recepción
     */
    @Query("""
            SELECT pr FROM PedidoReabastecimiento pr 
            WHERE pr.estado IN ('APROBADO', 'ORDENADO', 'RECIBIDO_PARCIAL')
            """)
    List<PedidoReabastecimiento> findPedidosPendientesRecepcion();

    /**
     * Obtiene los pedidos marcados como urgentes
     * que están en estado PENDIENTE o APROBADO.
     *
     * @return lista de pedidos urgentes
     */
    @Query("""
            SELECT pr FROM PedidoReabastecimiento pr 
            WHERE pr.prioridad = 'urgente' 
              AND pr.estado IN ('PENDIENTE', 'APROBADO')
            """)
    List<PedidoReabastecimiento> findPedidosUrgentes();

    /**
     * Obtiene todos los pedidos de reabastecimiento registrados dentro de un período,
     * ordenados por fecha de solicitud descendente.
     *
     * @param fechaInicio fecha inicial
     * @param fechaFin    fecha final
     * @return lista de pedidos dentro del rango
     */
    @Query("""
            SELECT pr FROM PedidoReabastecimiento pr 
            WHERE pr.fechaSolicitud BETWEEN :fechaInicio AND :fechaFin 
            ORDER BY pr.fechaSolicitud DESC
            """)
    List<PedidoReabastecimiento> findReabastecimientosPorPeriodo(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Cuenta cuántos pedidos existen en un determinado estado.
     *
     * @param estado estado del pedido
     * @return cantidad de pedidos en ese estado
     */
    long countByEstado(EstadoPedido estado);
}
