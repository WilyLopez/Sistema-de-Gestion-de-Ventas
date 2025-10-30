package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.dto.venta.VentaDTO;
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
 * Repositorio para la gestión de la entidad {@link Venta}.
 * <p>
 * Proporciona métodos CRUD básicos y consultas personalizadas
 * para atender los requerimientos funcionales:
 * <ul>
 *   <li>RF-007 – Registro de ventas</li>
 *   <li>RF-008 – Consulta y filtrado de ventas</li>
 *   <li>RF-009 – Anulación de ventas</li>
 *   <li>RF-014 – Reportes estadísticos de ventas</li>
 * </ul>
 * <p>
 * Incluye consultas derivadas y personalizadas en JPQL.
 *
 * @author
 *     Wilian Lopez
 * @version
 *     1.1 (2025)
 */
@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    /**
     * Busca una venta por su código único.
     *
     * @param codigoVenta Código de la venta (ej: "V-2024-00001").
     * @return Venta encontrada o {@link Optional#empty()} si no existe.
     */
    Optional<Venta> findByCodigoVenta(String codigoVenta);

    /**
     * Obtiene todas las ventas registradas entre dos fechas.
     *
     * @param inicio Fecha inicial del rango.
     * @param fin Fecha final del rango.
     * @return Lista de ventas en el período indicado.
     */
    List<Venta> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene las ventas realizadas por un cliente específico.
     *
     * @param idCliente Identificador del cliente.
     * @param pageable Parámetros de paginación.
     * @return Página de ventas del cliente.
     */
    Page<Venta> findByClienteIdCliente(Long idCliente, Pageable pageable);

    /**
     * Obtiene las ventas registradas por un vendedor (usuario) específico.
     *
     * @param idUsuario Identificador del usuario (vendedor).
     * @param pageable Parámetros de paginación.
     * @return Página de ventas realizadas por el usuario.
     */
    Page<Venta> findByUsuarioIdUsuario(Long idUsuario, Pageable pageable);

    /**
     * Filtra las ventas por su estado.
     *
     * @param estado Estado de la venta (Ej: PAGADO, ANULADO, etc.).
     * @param pageable Parámetros de paginación.
     * @return Página de ventas con el estado indicado.
     */
    Page<Venta> findByEstado(EstadoVenta estado, Pageable pageable);

    /**
     * Filtra las ventas por método de pago.
     *
     * @param idMetodoPago Identificador del método de pago.
     * @param pageable Parámetros de paginación.
     * @return Página de ventas filtradas por método de pago.
     */
    Page<Venta> findByMetodoPagoIdMetodoPago(Long idMetodoPago, Pageable pageable);

    /**
     * Obtiene las ventas dentro de un rango de fechas.
     *
     * @param fechaInicio Fecha inicial.
     * @param fechaFin Fecha final.
     * @param pageable Parámetros de paginación.
     * @return Página de ventas dentro del período.
     */
    Page<Venta> findByFechaCreacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);

    /**
     * Búsqueda avanzada de ventas por múltiples filtros opcionales.
     */
    @Query(value = """
    SELECT 
        v.idventa as idVenta,
        v.codigoventa as codigoVenta,
        c.idcliente as idCliente,
        c.nombre as nombreCliente,
        u.idusuario as idUsuario,
        u.nombre as nombreUsuario,
        v.fechacreacion as fechaCreacion,
        v.subtotal,
        v.igv,
        v.total,
        m.idmetodopago as idMetodoPago,
        m.nombre as nombreMetodoPago,
        v.estado,
        v.tipocomprobante as tipoComprobante,
        v.observaciones
    FROM venta v 
    JOIN cliente c ON c.idcliente = v.idcliente 
    JOIN usuario u ON u.idusuario = v.idusuario 
    JOIN metodopago m ON m.idmetodopago = v.idmetodopago 
    WHERE 
        (CAST(:codigoVenta AS TEXT) IS NULL OR v.codigoventa LIKE CONCAT('%', CAST(:codigoVenta AS TEXT), '%'))
        AND (CAST(:idCliente AS BIGINT) IS NULL OR c.idcliente = CAST(:idCliente AS BIGINT))
        AND (CAST(:idUsuario AS BIGINT) IS NULL OR u.idusuario = CAST(:idUsuario AS BIGINT))
        AND (CAST(:estado AS TEXT) IS NULL OR v.estado = CAST(:estado AS TEXT))
        AND (CAST(:idMetodoPago AS BIGINT) IS NULL OR m.idmetodopago = CAST(:idMetodoPago AS BIGINT))
        AND (CAST(:fechaInicio AS TIMESTAMP) IS NULL OR v.fechacreacion >= CAST(:fechaInicio AS TIMESTAMP))
        AND (CAST(:fechaFin AS TIMESTAMP) IS NULL OR v.fechacreacion <= CAST(:fechaFin AS TIMESTAMP))
    ORDER BY v.fechacreacion DESC
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM venta v 
    JOIN cliente c ON c.idcliente = v.idcliente 
    JOIN usuario u ON u.idusuario = v.idusuario 
    JOIN metodopago m ON m.idmetodopago = v.idmetodopago 
    WHERE 
        (CAST(:codigoVenta AS TEXT) IS NULL OR v.codigoventa LIKE CONCAT('%', CAST(:codigoVenta AS TEXT), '%'))
        AND (CAST(:idCliente AS BIGINT) IS NULL OR c.idcliente = CAST(:idCliente AS BIGINT))
        AND (CAST(:idUsuario AS BIGINT) IS NULL OR u.idusuario = CAST(:idUsuario AS BIGINT))
        AND (CAST(:estado AS TEXT) IS NULL OR v.estado = CAST(:estado AS TEXT))
        AND (CAST(:idMetodoPago AS BIGINT) IS NULL OR m.idmetodopago = CAST(:idMetodoPago AS BIGINT))
        AND (CAST(:fechaInicio AS TIMESTAMP) IS NULL OR v.fechacreacion >= CAST(:fechaInicio AS TIMESTAMP))
        AND (CAST(:fechaFin AS TIMESTAMP) IS NULL OR v.fechacreacion <= CAST(:fechaFin AS TIMESTAMP))
    """,
            nativeQuery = true)
    Page<Object[]> buscarVentasDTOConFiltrosNativo(
            @Param("codigoVenta") String codigoVenta,
            @Param("idCliente") Long idCliente,
            @Param("idUsuario") Long idUsuario,
            @Param("estado") String estado,
            @Param("idMetodoPago") Long idMetodoPago,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable
    );

    /**
     * Verifica si una venta puede ser anulada (dentro de las últimas 24 horas y con estado PAGADO).
     */
    @Query("""
           SELECT v FROM Venta v
           WHERE v.idVenta = :idVenta
             AND v.estado = 'PAGADO'
             AND v.fechaCreacion >= :fechaLimite
           """)
    Optional<Venta> findVentaAnulable(@Param("idVenta") Long idVenta,
                                      @Param("fechaLimite") LocalDateTime fechaLimite);

    /**
     * Obtiene las ventas realizadas en un período específico.
     */
    @Query("""
           SELECT v FROM Venta v
           WHERE v.fechaCreacion BETWEEN :fechaInicio AND :fechaFin
           ORDER BY v.fechaCreacion DESC
           """)
    List<Venta> findVentasPorPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio,
                                     @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Obtiene el monto total de ventas en un período (solo PAGADAS).
     */
    @Query("""
           SELECT COALESCE(SUM(v.total), 0)
           FROM Venta v
           WHERE v.fechaCreacion BETWEEN :fechaInicio AND :fechaFin
             AND v.estado = 'PAGADO'
           """)
    BigDecimal getTotalVentasPorPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio,
                                        @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Cuenta la cantidad total de ventas registradas en un período (solo PAGADAS).
     */
    @Query("""
           SELECT COUNT(v)
           FROM Venta v
           WHERE v.fechaCreacion BETWEEN :fechaInicio AND :fechaFin
             AND v.estado = 'PAGADO'
           """)
    Long countVentasPorPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio,
                               @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Obtiene el total de ventas agrupado por categoría de producto en un período dado.
     */
    @Query("""
           SELECT dv.producto.categoria.nombre, SUM(dv.subtotal)
           FROM DetalleVenta dv
           WHERE dv.venta.fechaCreacion BETWEEN :fechaInicio AND :fechaFin
             AND dv.venta.estado = 'PAGADO'
           GROUP BY dv.producto.categoria.nombre
           """)
    List<Object[]> getVentasPorCategoria(@Param("fechaInicio") LocalDateTime fechaInicio,
                                         @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Obtiene los productos más vendidos (por cantidad) en un período dado.
     */
    @Query("""
           SELECT dv.producto.nombre, SUM(dv.cantidad) as totalVendido
           FROM DetalleVenta dv
           WHERE dv.venta.fechaCreacion BETWEEN :fechaInicio AND :fechaFin
             AND dv.venta.estado = 'PAGADO'
           GROUP BY dv.producto.idProducto, dv.producto.nombre
           ORDER BY totalVendido DESC
           """)
    List<Object[]> getTopProductosVendidos(@Param("fechaInicio") LocalDateTime fechaInicio,
                                           @Param("fechaFin") LocalDateTime fechaFin,
                                           Pageable pageable);

    /**
     * Verifica si existen ventas asociadas a un cliente específico.
     */
    boolean existsByClienteIdCliente(Long idCliente);

    /**
     * Verifica si existen ventas registradas por un usuario específico.
     */
    boolean existsByUsuarioIdUsuario(Long idUsuario);
}
