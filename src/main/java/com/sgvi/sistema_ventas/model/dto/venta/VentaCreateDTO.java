package com.sgvi.sistema_ventas.model.dto.venta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para la creación de una nueva venta.
 * Incluye validaciones para los datos de entrada.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaCreateDTO {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long idCliente;

    @NotNull(message = "El ID del usuario/vendedor es obligatorio")
    private Long idUsuario;  // ← CAMBIAR DE idVendedor A idUsuario

    @NotNull(message = "El ID del método de pago es obligatorio")
    private Long idMetodoPago;

    @NotNull(message = "El tipo de comprobante es obligatorio")
    private String tipoComprobante;

    private BigDecimal subtotal;
    private BigDecimal descuentoTotal;
    private BigDecimal total;

    @Size(max = 500, message = "Las observaciones no pueden exceder los 500 caracteres")
    private String observaciones;

    @Valid
    @NotEmpty(message = "La venta debe tener al menos un detalle")
    private List<DetalleVentaDTO> detalles;
}