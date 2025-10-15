package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para la entidad Comprobante.
 * Proporciona métodos para gestionar comprobantes fiscales
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Repository
public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {

    // Buscar por venta
    Optional<Comprobante> findByVentaId(Long idVenta);

    // Verificar existencia de serie y número
    boolean existsBySerieAndNumero(String serie, String numero);

    // Obtener último número por serie
    @Query("SELECT MAX(c.numero) FROM Comprobante c WHERE c.serie = :serie")
    Optional<String> findLastNumeroBySerie(@Param("serie") String serie);

    // Buscar por número completo
    @Query("SELECT c FROM Comprobante c WHERE CONCAT(c.serie, '-', c.numero) = :numeroCompleto")
    Optional<Comprobante> findByNumeroCompleto(@Param("numeroCompleto") String numeroCompleto);
}