package com.sgvi.sistema_ventas.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Representa la tabla Categoria.
 * Clasifica los productos (e.g., 'Camisetas', 'Pantalones').
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categoria")
public class Categoria {

    /** Identificador único de la categoría (Clave Primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcategoria")
    private Integer idCategoria;

    /** Nombre único de la categoría. */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Descripción detallada de la categoría. */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /** Estado de la categoría (Activo/Inactivo). */
    @Column(name = "estado")
    private Boolean estado = true;

    /** Fecha de creación del registro. */
    @Column(name = "fechacreacion", updatable = false)
    private LocalDateTime fechaCreacion;

    /** Fecha de la última actualización del registro. */
    @Column(name = "fechaactualizacion")
    private LocalDateTime fechaActualizacion;
}