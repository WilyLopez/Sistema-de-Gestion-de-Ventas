package com.sgvi.sistema_ventas.model.dto.pedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para crear un nuevo pedido
 * Request para POST /api/pedidos
 *
 * @author Wilian Lopez
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearPedidoDTO {

    @NotNull(message = "El ID del cliente es obligatorio")
    @Positive(message = "El ID del cliente debe ser positivo")
    private Long idCliente;

    @NotNull(message = "El ID del usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser positivo")
    private Long idUsuario;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaEntrega;

    @NotBlank(message = "La dirección de envío es obligatoria")
    @Size(min = 10, max = 255, message = "La dirección debe tener entre 10 y 255 caracteres")
    private String direccionEnvio;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    @NotEmpty(message = "Debe incluir al menos un producto en el pedido")
    @Valid
    private List<DetallePedidoDTO> detalles;
}