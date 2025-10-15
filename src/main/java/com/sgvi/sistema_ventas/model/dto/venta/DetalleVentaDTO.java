package com.sgvi.sistema_ventas.model.dto.venta;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO para representar un detalle de venta.
 * Se utiliza tanto en creación como en respuestas de ventas.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVentaDTO {

    private Long idDetalle;

    @NotNull(message = "El ID del producto es obligatorio")
    private Long idProducto;

    private String codigoProducto;
    private String nombreProducto;
    private String marca;
    private String talla;
    private String color;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio unitario debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El precio unitario debe tener máximo 2 decimales")
    private BigDecimal precioUnitario;

    @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El descuento debe tener máximo 2 decimales")
    private BigDecimal descuento;

    @NotNull(message = "El subtotal es obligatorio")
    @DecimalMin(value = "0.0", message = "El subtotal no puede ser negativo")
    @Digits(integer = 12, fraction = 2, message = "El subtotal debe tener máximo 2 decimales")
    private BigDecimal subtotal;

    // Método para calcular subtotal
    public void calcularSubtotal() {
        if (cantidad != null && precioUnitario != null) {
            BigDecimal totalSinDescuento = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
            this.subtotal = totalSinDescuento.subtract(descuento != null ? descuento : BigDecimal.ZERO);

            if (this.subtotal.compareTo(BigDecimal.ZERO) < 0) {
                this.subtotal = BigDecimal.ZERO;
            }
        }
    }
}