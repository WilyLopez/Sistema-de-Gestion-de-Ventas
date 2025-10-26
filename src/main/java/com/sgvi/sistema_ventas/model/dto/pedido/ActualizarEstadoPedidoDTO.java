package com.sgvi.sistema_ventas.model.dto.pedido;

import com.sgvi.sistema_ventas.model.enums.EstadoPedido;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar el estado de un pedido
 * Request para PUT /api/pedidos/{id}/estado
 *
 * @author Wilian Lopez
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarEstadoPedidoDTO {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoPedido nuevoEstado;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;
}