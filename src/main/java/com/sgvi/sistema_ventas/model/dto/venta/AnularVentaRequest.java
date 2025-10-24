package com.sgvi.sistema_ventas.model.dto.venta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para anular una venta.
 * Requiere motivo obligatorio de anulación.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnularVentaRequest {

    @NotBlank(message = "El motivo de anulación es obligatorio")
    @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
    private String motivo;
}