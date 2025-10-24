package com.sgvi.sistema_ventas.model.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO para criterios de búsqueda y filtrado de Usuarios.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioBusquedaDTO {

    @Size(max = 100, message = "El texto de búsqueda es demasiado largo")
    private String texto;

    private Boolean estado;

    @Positive(message = "El ID del rol debe ser positivo")
    private Long idRol;
}