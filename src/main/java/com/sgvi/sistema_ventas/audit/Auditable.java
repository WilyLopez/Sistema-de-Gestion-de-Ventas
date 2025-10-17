package com.sgvi.sistema_ventas.audit;

import java.time.LocalDateTime;

/**
 * Interface para entidades auditables.
 * Proporciona métodos para tracking de creación y actualización.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public interface Auditable {

    /**
     * Obtiene la fecha de creación
     */
    LocalDateTime getFechaCreacion();

    /**
     * Establece la fecha de creación
     */
    void setFechaCreacion(LocalDateTime fechaCreacion);

    /**
     * Obtiene la fecha de última actualización
     */
    LocalDateTime getFechaActualizacion();

    /**
     * Establece la fecha de última actualización
     */
    void setFechaActualizacion(LocalDateTime fechaActualizacion);

    /**
     * Callback ejecutado antes de persistir la entidad
     */
    default void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        setFechaCreacion(now);
        setFechaActualizacion(now);
    }

    /**
     * Callback ejecutado antes de actualizar la entidad
     */
    default void preUpdate() {
        setFechaActualizacion(LocalDateTime.now());
    }
}
