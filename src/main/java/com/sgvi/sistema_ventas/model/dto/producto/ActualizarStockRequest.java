package com.sgvi.sistema_ventas.model.dto.producto;

import com.sgvi.sistema_ventas.model.enums.TipoMovimiento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar stock de producto manualmente.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarStockRequest {

    @NotNull(message = "La cantidad es obligatoria")
    private Integer cantidad;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimiento tipoMovimiento;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(min = 5, max = 200, message = "El motivo debe tener entre 5 y 200 caracteres")
    private String motivo;
}