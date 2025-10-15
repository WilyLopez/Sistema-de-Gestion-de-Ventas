package com.sgvi.sistema_ventas.model.dto.venta;

import com.sgvi.sistema_ventas.model.enums.EstadoVenta;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para representar un resumen de venta.
 * Optimizado para listados y consultas rápidas.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaResumenDTO {
    private Long idVenta;
    private String codigoVenta;
    private String nombreCliente;
    private String nombreUsuario;
    private LocalDateTime fechaVenta;
    private BigDecimal total;
    private EstadoVenta estado;
    private String metodoPago;
    private String comprobante;
    private Integer cantidadProductos;

    // Método para formatear información de display
    public String getDisplayInfo() {
        return String.format("%s - %s - S/. %s",
                codigoVenta,
                nombreCliente,
                total.toString());
    }
}