package com.sgvi.sistema_ventas.model.dto.devolucion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearDevolucionRequestDTO {

    @NotNull(message = "El ID de la venta es obligatorio")
    @Positive(message = "El ID de la venta debe ser un número positivo")
    private Long idVenta;

    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser un número positivo")
    private Long idUsuario;

    @NotBlank(message = "El motivo de la devolución es obligatorio")
    @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
    private String motivo;

    @Valid
    @NotEmpty(message = "Debe especificar al menos un producto a devolver")
    @Size(max = 50, message = "No se pueden devolver más de 50 productos diferentes en una solicitud")
    private List<DetalleDevolucionRequestDTO> detalles;
}
