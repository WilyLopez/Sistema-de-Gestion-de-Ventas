package com.sgvi.sistema_ventas.model.dto.pedido;


import com.sgvi.sistema_ventas.model.enums.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta con información completa del pedido
 * Response para GET /api/pedidos/{id}
 *
 * @author Wilian Lopez
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoResponseDTO {

    private Long idPedido;
    private String codigoPedido;

    // Información del cliente
    private ClienteBasicoDTO cliente;

    // Información del usuario que creó el pedido
    private UsuarioBasicoDTO usuario;

    // Fechas
    private LocalDateTime fechaPedido;
    private LocalDateTime fechaEntrega;

    // Estado
    private EstadoPedido estado;
    private String estadoDescripcion;
    private String estadoBadgeClass;

    // Montos
    private BigDecimal subtotal;
    private BigDecimal total;

    // Información adicional
    private String direccionEnvio;
    private String observaciones;

    // Detalles del pedido
    private List<DetallePedidoResponseDTO> detalles;
    private Integer totalProductos;

    // Flags de estado
    private Boolean puedeCancelarse;
    private Boolean puedeModificarse;
    private Boolean esEstadoFinal;
}