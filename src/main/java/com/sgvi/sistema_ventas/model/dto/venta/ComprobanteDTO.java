package com.sgvi.sistema_ventas.model.dto.venta;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para representar un comprobante asociado a una venta.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteDTO {
    private Long idComprobante;
    private String tipo;
    private String serie;
    private String numero;
    private String numeroCompleto;
    private LocalDateTime fechaEmision;
    private String rucEmisor;
    private String razonSocialEmisor;
    private String direccionEmisor;
    private BigDecimal igv;
    private BigDecimal total;
    private String estado;

    // Método para generar número completo
    public String getNumeroCompleto() {
        if (serie != null && numero != null) {
            return String.format("%s-%s", serie, numero);
        }
        return null;
    }
}