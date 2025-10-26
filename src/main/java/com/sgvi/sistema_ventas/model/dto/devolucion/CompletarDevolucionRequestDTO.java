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
public class CompletarDevolucionRequestDTO {

    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser un número positivo")
    private Long idUsuario;

    // Opcional: notas adicionales al completar
    @Size(max = 300, message = "Las notas no deben exceder los 300 caracteres")
    private String notas;
}