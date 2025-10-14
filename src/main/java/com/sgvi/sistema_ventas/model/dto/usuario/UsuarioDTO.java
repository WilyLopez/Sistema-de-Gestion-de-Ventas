package com.sgvi.sistema_ventas.model.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de un Usuario.
 * Contiene todos los datos relevantes, excluyendo la contraseña.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {

    /** Identificador único del usuario. */
    private Integer idUsuario;

    /** Nombre de usuario para login. */
    private String username;

    /** Nombre completo del usuario. */
    private String nombre;

    /** Apellido del usuario. */
    private String apellido;

    /** Correo electrónico. */
    private String correo;

    /** Teléfono de contacto. */
    private String telefono;

    /** Dirección del usuario. */
    private String direccion;

    /** Estado del usuario (true=Activo, false=Inactivo). */
    private Boolean estado;

    /** Identificador del Rol. */
    private Integer idRol;

    /** Nombre del Rol. */
    private String nombreRol;

    /** Fecha de creación del registro. */
    private LocalDateTime fechaCreacion;

    /** Último inicio de sesión. */
    private LocalDateTime ultimoLogin;
}