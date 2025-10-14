package com.sgvi.sistema_ventas.model.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Representa la tabla Usuario.
 * Almacena los datos de los empleados que acceden al sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "usuario")
public class Usuario {

    /** Identificador único del usuario (Clave Primaria). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idusuario")
    private Integer idUsuario;

    /** Nombre de usuario único para inicio de sesión. */
    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    /** Nombre del usuario. */
    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    /** Apellido del usuario. */
    @Column(name = "apellido", nullable = false, length = 50)
    private String apellido;

    /** Correo electrónico único del usuario. */
    @Column(name = "correo", nullable = false, length = 100, unique = true)
    private String correo;

    /** Contraseña hasheada (Campo sensible). */
    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;

    /** Teléfono de contacto. */
    @Column(name = "telefono", length = 20)
    private String telefono;

    /** Dirección del usuario. */
    @Column(name = "direccion", length = 150)
    private String direccion;

    /** Estado del usuario (Activo/Inactivo). */
    @Column(name = "estado")
    private Boolean estado = true;

    /** Clave foránea al Rol. */
    @Column(name = "idrol", nullable = false)
    private Integer idRol;

    /** Fecha de creación del registro. */
    @Column(name = "fechacreacion", updatable = false)
    private LocalDateTime fechaCreacion;

    /** Fecha de la última actualización del registro. */
    @Column(name = "fechaactualizacion")
    private LocalDateTime fechaActualizacion;

    /** Marca de tiempo del último inicio de sesión. */
    @Column(name = "ultimologin")
    private LocalDateTime ultimoLogin;

    /**
     * Relación ManyToOne con Rol.
     * Indica el rol que desempeña este usuario en el sistema.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idrol", insertable = false, updatable = false)
    private Rol rol;
}