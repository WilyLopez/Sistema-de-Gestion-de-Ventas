package com.sgvi.sistema_ventas.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Representa la tabla RolPermiso (Tabla de Unión).
 * Mapea la relación Many-to-Many entre Rol y Permiso.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "rolpermiso")
@IdClass(RolPermiso.RolPermisoId.class) // Clase para manejar la clave compuesta
public class RolPermiso implements Serializable {

    /** Clave foránea al Rol. Parte de la Clave Compuesta. */
    @Id
    @Column(name = "idrol")
    private Integer idRol;

    /** Clave foránea al Permiso. Parte de la Clave Compuesta. */
    @Id
    @Column(name = "idpermiso")
    private Integer idPermiso;

    // --- Relaciones (Opcional, si se desea navegar desde aquí) ---

    /** Relación ManyToOne con Rol. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idrol", insertable = false, updatable = false)
    private Rol rol;

    /** Relación ManyToOne con Permiso. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idpermiso", insertable = false, updatable = false)
    private Permiso permiso;

    /**
     * Clase embebida para representar la Clave Primaria Compuesta.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolPermisoId implements Serializable {
        private Integer idRol;
        private Integer idPermiso;
    }
}