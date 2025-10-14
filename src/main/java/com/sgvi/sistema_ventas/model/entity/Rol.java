package com.sgvi.sistema_ventas.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Representa la tabla Rol.
 * Define los diferentes roles de usuario dentro del sistema (e.g., administrador, vendedor).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "rol")

public class Rol {

    /** Identificador único del rol (Clave Primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idrol")
    private Integer idRol;

    /** Nombre único del rol (e.g., 'administrador', 'vendedor'). */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Descripción detallada del rol. */
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    /** Nivel de acceso asociado al rol (1 a 10), usado para jerarquía. */
    @Column(name = "nivelacceso", nullable = false)
    private Integer nivelAcceso;

    /** Estado del rol (Activo/Inactivo). */
    @Column(name = "estado")
    private Boolean estado = true;

    /** Fecha de creación del registro. */
    @Column(name = "fechacreacion", updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Relación ManyToMany con Permiso a través de la tabla intermedia RolPermiso.
     * Define los permisos que tiene este rol.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rolpermiso",
            joinColumns = @JoinColumn(name = "idrol"),
            inverseJoinColumns = @JoinColumn(name = "idpermiso")
    )
    private Set<Permiso> permisos;
}