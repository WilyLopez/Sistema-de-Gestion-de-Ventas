package com.sgvi.sistema_ventas.model.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

/**
 * DTO para la creación de un nuevo Usuario.
 * Incluye validaciones básicas de campos obligatorios.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioCreateDTO {

    /** Nombre de usuario único. */
    @NotBlank(message = "El username es obligatorio.")
    @Size(min = 4, max = 50, message = "El username debe tener entre 4 y 50 caracteres.")
    private String username;

    /** Nombre del usuario. */
    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres.")
    private String nombre;

    /** Apellido del usuario. */
    @NotBlank(message = "El apellido es obligatorio.")
    @Size(max = 50, message = "El apellido no puede exceder los 50 caracteres.")
    private String apellido;

    /** Correo electrónico único. */
    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "El formato del correo es inválido.")
    @Size(max = 100, message = "El correo no puede exceder los 100 caracteres.")
    private String correo;

    /** Contraseña inicial. */
    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 8, max = 255, message = "La contraseña debe tener al menos 8 caracteres.")
    private String contrasena;

    /** Teléfono de contacto (Opcional). */
    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres.")
    private String telefono;

    /** Dirección del usuario (Opcional). */
    @Size(max = 150, message = "La dirección no puede exceder los 150 caracteres.")
    private String direccion;

    /** ID del rol asignado. */
    @NotNull(message = "El rol es obligatorio.")
    @Positive(message = "El ID del rol debe ser un número positivo.")
    private Integer idRol;

    /** Estado inicial (por defecto, activo). */
    private Boolean estado = true;
}