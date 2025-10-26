package com.sgvi.sistema_ventas.model.dto.devolucion;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleDevolucionRequestDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    @Positive(message = "El ID del producto debe ser un número positivo")
    private Long idProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Max(value = 1000, message = "La cantidad no puede exceder 1000 unidades")
    private Integer cantidad;

    @Size(max = 300, message = "El motivo específico no debe exceder los 300 caracteres")
    private String motivo;
}