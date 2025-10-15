package com.sgvi.sistema_ventas.model.dto.venta;

import com.sgvi.sistema_ventas.model.enums.EstadoVenta;
import com.sgvi.sistema_ventas.model.enums.TipoComprobante;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO que representa una venta completa en el sistema.
 * Se utiliza para enviar datos de ventas en respuestas API.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaDTO {
    private Long idVenta;
    private String codigoVenta;
    private Long idCliente;
    private String nombreCliente;
    private Long idUsuario;
    private String nombreUsuario;
    private LocalDateTime fechaVenta;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private Long idMetodoPago;
    private String metodoPago;
    private EstadoVenta estado;
    private TipoComprobante tipoComprobante;
    private String observaciones;
    private LocalDateTime fechaCreacion;
    private List<DetalleVentaDTO> detalles;
    private ComprobanteDTO comprobante;
}