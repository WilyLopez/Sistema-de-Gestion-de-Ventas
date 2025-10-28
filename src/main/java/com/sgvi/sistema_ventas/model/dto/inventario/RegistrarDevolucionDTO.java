package com.sgvi.sistema_ventas.model.dto.inventario;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para registrar devolución de productos
 * Request para POST /api/inventario/devolucion
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarDevolucionDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    @Positive(message = "El ID del producto debe ser positivo")
    private Long idProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Max(value = 10000, message = "La cantidad no puede exceder 10000 unidades")
    private Integer cantidad;

    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser positivo")
    private Long idUsuario;

    @NotBlank(message = "La observación es obligatoria para devoluciones")
    @Size(min = 10, max = 500, message = "La observación debe tener entre 10 y 500 caracteres")
    private String observacion;
}