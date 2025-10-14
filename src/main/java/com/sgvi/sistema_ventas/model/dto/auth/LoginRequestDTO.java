package com.sgvi.sistema_ventas.model.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de inicio de sesión.
 * Contiene las credenciales del usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {

    /** Nombre de usuario o correo electrónico. */
    @NotBlank(message = "El nombre de usuario/correo no puede estar vacío.")
    private String username;

    /** Contraseña del usuario. */
    @NotBlank(message = "La contraseña no puede estar vacía.")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    private String password;
}