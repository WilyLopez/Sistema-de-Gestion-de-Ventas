package com.sgvi.sistema_ventas.model.dto.inventario;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para registrar entrada de productos al inventario
 * Request para POST /api/inventario/entrada
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarEntradaDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    @Positive(message = "El ID del producto debe ser positivo")
    private Long idProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Max(value = 100000, message = "La cantidad no puede exceder 100000 unidades")
    private Integer cantidad;

    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser positivo")
    private Long idUsuario;

    @Size(max = 500, message = "La observación no puede exceder 500 caracteres")
    private String observacion;
}