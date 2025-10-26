package com.sgvi.sistema_ventas.model.dto.devolucion;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleDevolucionResponseDTO {

    private Long idDetalleDevolucion;
    private Long idProducto;
    private String nombreProducto;
    private String codigoProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private String motivo;
}