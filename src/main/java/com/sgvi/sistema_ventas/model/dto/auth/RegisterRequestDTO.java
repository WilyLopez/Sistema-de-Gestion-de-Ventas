package com.sgvi.sistema_ventas.model.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 50, message = "Username debe tener entre 3 y 50 caracteres")
    private String username;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "Nombre máximo 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50, message = "Apellido máximo 50 caracteres")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "Email máximo 100 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "Contraseña debe tener al menos 6 caracteres")
    private String password;

    @Size(max = 20, message = "Teléfono máximo 20 caracteres")
    private String telefono;

    @Size(max = 150, message = "Dirección máximo 150 caracteres")
    private String direccion;
}