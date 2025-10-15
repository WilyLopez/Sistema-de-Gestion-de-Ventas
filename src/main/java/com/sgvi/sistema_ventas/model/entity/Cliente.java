package com.sgvi.sistema_ventas.model.entity;

import com.sgvi.sistema_ventas.model.enums.TipoDocumento;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa la tabla Cliente.
 * Almacena la información de los clientes de la tienda.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cliente", uniqueConstraints = {
        @UniqueConstraint(name = "unique_tipo_numero_documento", columnNames = {"tipodocumento", "numerodocumento"})
})
public class Cliente {

    /** Identificador único del cliente (Clave Primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcliente")
    private Long idCliente;

    /**
     * Tipo de documento de identidad.
     * Mapeado como String en la BD, usando el Enum TipoDocumento.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipodocumento", length = 10)
    private TipoDocumento tipoDocumento = TipoDocumento.DNI;

    /** Número del documento de identidad (parte de la Clave Única Compuesta). */
    @Column(name = "numerodocumento", nullable = false, length = 20)
    private String numeroDocumento;

    /** Nombre del cliente. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /** Apellido del cliente. */
    @Column(name = "apellido", nullable = false, length = 50)
    private String apellido;

    /** Correo electrónico único del cliente. */
    @Column(name = "correo", length = 100, unique = true)
    private String correo;

    /** Número de teléfono. */
    @Column(name = "telefono", length = 20)
    private String telefono;

    /** Dirección de residencia o envío. */
    @Column(name = "direccion", length = 150)
    private String direccion;

    /** Fecha de nacimiento del cliente. */
    @Column(name = "fechanacimiento")
    private LocalDate fechaNacimiento;

    /** Estado del cliente (Activo/Inactivo). */
    @Column(name = "estado")
    private Boolean estado = true;

    /** Fecha de registro del cliente en el sistema. */
    @Column(name = "fecharegistro", updatable = false)
    private LocalDateTime fechaRegistro;
}