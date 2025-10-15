package com.sgvi.sistema_ventas.model.entity;

import lombok.*;
import jakarta.persistence.*;

/**
 * Representa la tabla MetodoPago.
 * Define las formas de pago aceptadas en la tienda (e.g., 'Efectivo', 'Tarjeta Crédito').
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "metodopago")
public class MetodoPago {

    /** Identificador único del método de pago (Clave Primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idmetodopago")
    private Long idMetodoPago;

    /** Nombre único del método de pago. */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /** Descripción breve. */
    @Column(name = "descripcion", length = 100)
    private String descripcion;

    /** Estado del método de pago (Activo/Inactivo). */
    @Column(name = "estado")
    private Boolean estado = true;
}