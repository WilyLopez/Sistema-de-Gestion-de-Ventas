package com.sgvi.sistema_ventas.repository;

import com.sgvi.sistema_ventas.model.entity.ConfiguracionAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para la entidad ConfiguracionAlerta.
 * Proporciona métodos para gestionar configuración de alertas según RF-011
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Repository
public interface ConfiguracionAlertaRepository extends JpaRepository<ConfiguracionAlerta, Long> {

    // Buscar por tipo de alerta
    Optional<ConfiguracionAlerta> findByTipoAlerta(String tipoAlerta);

    // Buscar configuraciones activas
    java.util.List<ConfiguracionAlerta> findByActivoTrue();

    // Verificar existencia por tipo
    boolean existsByTipoAlerta(String tipoAlerta);
}