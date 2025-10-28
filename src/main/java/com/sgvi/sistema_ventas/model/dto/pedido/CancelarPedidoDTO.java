package com.sgvi.sistema_ventas.model.dto.pedido;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cancelar un pedido
 * Request para PUT /api/pedidos/{id}/cancelar
 *
 * @author Wilian Lopez
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelarPedidoDTO {

    @NotBlank(message = "El motivo de cancelación es obligatorio")
    @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
    private String motivo;
}