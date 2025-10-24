package com.sgvi.sistema_ventas.model.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO para la actualización de un Usuario existente.
 * Permite la actualización parcial de campos. Username NO se puede actualizar.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioUpdateDTO {

    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
    private String nombre;

    @Size(max = 50, message = "El apellido no puede exceder los 50 caracteres")
    private String apellido;

    @Email(message = "El formato del correo es inválido")
    @Size(max = 100, message = "El correo no puede exceder los 100 caracteres")
    private String correo;

    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
    private String telefono;

    @Size(max = 150, message = "La dirección no puede exceder los 150 caracteres")
    private String direccion;

    @Positive(message = "El ID del rol debe ser un número positivo")
    private Long idRol;

    private Boolean estado;
}