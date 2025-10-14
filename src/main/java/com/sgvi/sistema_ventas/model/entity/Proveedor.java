package com.sgvi.sistema_ventas.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Representa la tabla Proveedor.
 * Almacena información de las empresas o personas que suministran los productos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "proveedor")
public class Proveedor {

    /** Identificador único del proveedor (Clave Primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idproveedor")
    private Integer idProveedor;

    /** Número de RUC (Registro Único de Contribuyentes) único del proveedor. */
    @Column(name = "ruc", length = 15, unique = true)
    private String ruc;

    /** Nombre legal o razón social del proveedor. */
    @Column(name = "razonsocial", nullable = false, length = 150)
    private String razonSocial;

    /** Dirección física del proveedor. */
    @Column(name = "direccion", length = 200)
    private String direccion;

    /** Número de teléfono del proveedor. */
    @Column(name = "telefono", length = 20)
    private String telefono;

    /** Correo electrónico de contacto. */
    @Column(name = "correo", length = 100)
    private String correo;

    /** Estado del proveedor (Activo/Inactivo). */
    @Column(name = "estado")
    private Boolean estado = true;

    /** Fecha de creación del registro. */
    @Column(name = "fechacreacion", updatable = false)
    private LocalDateTime fechaCreacion;
}