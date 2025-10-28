package com.sgvi.sistema_ventas.model.dto.devolucion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AprobarDevolucionRequestDTO {

    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser un número positivo")
    private Long idUsuario;

    // Opcional: comentario de aprobación
    @Size(max = 300, message = "El comentario no debe exceder los 300 caracteres")
    private String comentario;
}