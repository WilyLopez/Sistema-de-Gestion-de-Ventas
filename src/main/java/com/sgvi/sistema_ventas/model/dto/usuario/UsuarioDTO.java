package com.sgvi.sistema_ventas.model.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de un Usuario.
 * Contiene todos los datos relevantes, excluyendo la contraseña.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {

    private Long idUsuario;
    private String username;
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private String direccion;
    private Boolean estado;
    private Long idRol;
    private String nombreRol;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoLogin;
}