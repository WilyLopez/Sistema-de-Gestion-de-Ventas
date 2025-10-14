package com.sgvi.sistema_ventas.model.entity;

import lombok.*;
import jakarta.persistence.*;

/**
 * Representa la tabla Permiso.
 * Define una funcionalidad específica a la que un usuario puede tener acceso.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "permiso")
public class Permiso {

    /** Identificador único del permiso (Clave Primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idpermiso")
    private Integer idPermiso;

    /** Nombre único del permiso (e.g., 'venta.crear'). */
    @Column(name = "nombre", nullable = false, length = 100, unique = true)
    private String nombre;

    /** Descripción detallada del permiso. */
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    /** Módulo al que pertenece el permiso (e.g., 'Ventas', 'Usuarios'). */
    @Column(name = "modulo", nullable = false, length = 50)
    private String modulo;
}