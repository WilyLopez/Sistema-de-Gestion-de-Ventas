package com.sgvi.sistema_ventas.audit;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * Listener JPA para auditoría automática de entidades.
 * Se ejecuta automáticamente en operaciones de persistencia.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Slf4j
public class AuditListener {

    /**
     * Se ejecuta antes de persistir una nueva entidad
     */
    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof Auditable auditable) {
            LocalDateTime now = LocalDateTime.now();
            auditable.setFechaCreacion(now);
            auditable.setFechaActualizacion(now);

            log.debug("Auditoría PrePersist: {} - Fecha: {}",
                    entity.getClass().getSimpleName(), now);
        }
    }

    /**
     * Se ejecuta antes de actualizar una entidad existente
     */
    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof Auditable auditable) {
            LocalDateTime now = LocalDateTime.now();
            auditable.setFechaActualizacion(now);

            log.debug("Auditoría PreUpdate: {} - Fecha: {}",
                    entity.getClass().getSimpleName(), now);
        }
    }
}
