package com.sgvi.sistema_ventas.model.dto.inventario;

import com.sgvi.sistema_ventas.model.enums.TipoMovimiento;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO para representar un movimiento de inventario.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioMovimientoDTO {
    private Long idMovimiento;
    private Long idProducto;
    private String codigoProducto;
    private String nombreProducto;
    private TipoMovimiento tipoMovimiento;
    private Integer cantidad;
    private Integer stockAnterior;
    private Integer stockNuevo;
    private LocalDateTime fechaMovimiento;
    private Long idUsuario;
    private String nombreUsuario;
    private Long idVenta;
    private String codigoVenta;
    private String observacion;

    // Método para determinar el impacto en stock
    public String getImpactoStock() {
        if (tipoMovimiento != null && tipoMovimiento.getIncrementaStock() != null) {
            return tipoMovimiento.getIncrementaStock() ? "INCREMENTO" : "DECREMENTO";
        }
        return "AJUSTE";
    }
}