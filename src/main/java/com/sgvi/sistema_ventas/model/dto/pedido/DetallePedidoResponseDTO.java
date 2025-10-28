package com.sgvi.sistema_ventas.model.dto.pedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de respuesta para el detalle de un pedido
 * Se usa dentro de PedidoResponseDTO
 *
 * @author Wilian Lopez
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetallePedidoResponseDTO {

    private Long idDetallePedido;

    // Información del producto
    private Long idProducto;
    private String nombreProducto;
    private String codigoProducto;
    private String categoriaProducto;

    // Cantidades y precios
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    // Stock disponible al momento de consulta
    private Integer stockDisponible;
}