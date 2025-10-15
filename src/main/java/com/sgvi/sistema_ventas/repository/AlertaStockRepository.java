package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.AlertaStock;
import com.sgvi.sistema_ventas.model.enums.NivelUrgencia;
import com.sgvi.sistema_ventas.model.enums.TipoAlerta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para la entidad AlertaStock.
 * Proporciona métodos para gestionar alertas de stock según RF-011
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Repository
public interface AlertaStockRepository extends JpaRepository<AlertaStock, Long> {

    // RF-011: Alertas no leídas
    Page<AlertaStock> findByLeidaFalse(Pageable pageable);

    // RF-011: Alertas por producto
    Page<AlertaStock> findByProductoId(Long idProducto, Pageable pageable);

    // RF-011: Alertas por tipo
    Page<AlertaStock> findByTipoAlerta(TipoAlerta tipoAlerta, Pageable pageable);

    // RF-011: Alertas por nivel de urgencia
    Page<AlertaStock> findByNivelUrgencia(NivelUrgencia nivelUrgencia, Pageable pageable);

    // RF-011: Alertas por rango de fechas
    Page<AlertaStock> findByFechaAlertaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);

    // RF-011: Búsqueda combinada con filtros
    @Query("SELECT a FROM AlertaStock a WHERE " +
            "(:idProducto IS NULL OR a.producto.id = :idProducto) AND " +
            "(:tipoAlerta IS NULL OR a.tipoAlerta = :tipoAlerta) AND " +
            "(:nivelUrgencia IS NULL OR a.nivelUrgencia = :nivelUrgencia) AND " +
            "(:leida IS NULL OR a.leida = :leida) AND " +
            "(:fechaInicio IS NULL OR a.fechaAlerta >= :fechaInicio) AND " +
            "(:fechaFin IS NULL OR a.fechaAlerta <= :fechaFin)")
    Page<AlertaStock> buscarAlertasConFiltros(
            @Param("idProducto") Long idProducto,
            @Param("tipoAlerta") TipoAlerta tipoAlerta,
            @Param("nivelUrgencia") NivelUrgencia nivelUrgencia,
            @Param("leida") Boolean leida,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);

    // RF-011: Contar alertas no leídas por nivel de urgencia
    @Query("SELECT a.nivelUrgencia, COUNT(a) FROM AlertaStock a WHERE a.leida = false GROUP BY a.nivelUrgencia")
    List<Object[]> countAlertasNoLeidasPorUrgencia();

    // RF-011: Alertas críticas no leídas
    List<AlertaStock> findByNivelUrgenciaAndLeidaFalse(NivelUrgencia nivelUrgencia);

    // RF-011: Verificar si ya existe alerta similar no leída
    @Query("SELECT COUNT(a) > 0 FROM AlertaStock a WHERE a.producto.id = :idProducto AND a.tipoAlerta = :tipoAlerta AND a.leida = false")
    boolean existsAlertaSimilarNoLeida(@Param("idProducto") Long idProducto,
                                       @Param("tipoAlerta") TipoAlerta tipoAlerta);

    // RF-011: Marcar alertas como leídas
    @Modifying
    @Query("UPDATE AlertaStock a SET a.leida = true, a.fechaLectura = :fechaLectura, a.usuarioNotificado.id = :idUsuario WHERE a.id = :idAlerta")
    void marcarComoLeida(@Param("idAlerta") Long idAlerta,
                         @Param("fechaLectura") LocalDateTime fechaLectura,
                         @Param("idUsuario") Long idUsuario);

    // RF-014: Reporte de alertas por período
    @Query("SELECT a FROM AlertaStock a WHERE a.fechaAlerta BETWEEN :fechaInicio AND :fechaFin ORDER BY a.fechaAlerta DESC")
    List<AlertaStock> findAlertasPorPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio,
                                            @Param("fechaFin") LocalDateTime fechaFin);

    // Contar total de alertas no leídas
    long countByLeidaFalse();
}