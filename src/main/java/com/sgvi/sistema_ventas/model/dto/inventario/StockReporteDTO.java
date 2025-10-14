package com.sgvi.sistema_ventas.model.dto.inventario;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO para reportes de stock y inventario.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReporteDTO {
    private Long idProducto;
    private String codigoProducto;
    private String nombreProducto;
    private String categoria;
    private String marca;
    private String talla;
    private String color;
    private Integer stockActual;
    private Integer stockMinimo;
    private BigDecimal precioVenta;
    private BigDecimal valorTotalStock;
    private String estadoStock; // NORMAL, BAJO, AGOTADO, EXCESIVO

    // Método para calcular valor total
    public void calcularValorTotal() {
        if (stockActual != null && precioVenta != null) {
            this.valorTotalStock = precioVenta.multiply(BigDecimal.valueOf(stockActual));
        }
    }

    // Método para determinar estado de stock
    public String getEstadoStock() {
        if (stockActual == null || stockMinimo == null) {
            return "DESCONOCIDO";
        }

        if (stockActual == 0) {
            return "AGOTADO";
        } else if (stockActual <= stockMinimo) {
            return "BAJO";
        } else if (stockActual > stockMinimo * 3) {
            return "EXCESIVO";
        } else {
            return "NORMAL";
        }
    }
}