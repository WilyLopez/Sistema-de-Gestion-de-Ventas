package com.sgvi.sistema_ventas.model.dto.inventario;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para registrar ajuste de inventario
 * Request para POST /api/inventario/ajuste
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarAjusteDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    @Positive(message = "El ID del producto debe ser positivo")
    private Long idProducto;

    @NotNull(message = "El nuevo stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Max(value = 1000000, message = "El stock no puede exceder 1,000,000 unidades")
    private Integer nuevoStock;

    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser positivo")
    private Long idUsuario;

    @NotBlank(message = "La observación es obligatoria para ajustes")
    @Size(min = 10, max = 500, message = "La observación debe tener entre 10 y 500 caracteres")
    private String observacion;
}