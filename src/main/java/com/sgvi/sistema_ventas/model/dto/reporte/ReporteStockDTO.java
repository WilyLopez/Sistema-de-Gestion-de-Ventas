package com.sgvi.sistema_ventas.model.dto.reporte;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO para reportes de stock y niveles de inventario.
 * Proporciona análisis del estado actual del inventario.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteStockDTO {

    // Filtros
    private Long idCategoria;
    private Long idProveedor;
    private String estadoStock; // TODOS, NORMAL, BAJO, AGOTADO, EXCESIVO

    // Estadísticas generales
    private Integer totalProductos;
    private Integer productosConStock;
    private Integer productosStockBajo;
    private Integer productosAgotados;
    private Integer productosStockExcesivo;
    private BigDecimal valorTotalInventario;
    private BigDecimal valorInventarioDisponible;

    // Productos que requieren atención
    private Integer productosNecesitanReabastecimiento;
    private Integer alertasActivas;
    private Integer productosSinMovimiento;

    // Métodos calculados
    public BigDecimal getPorcentajeStockBajo() {
        if (totalProductos != null && totalProductos > 0 && productosStockBajo != null) {
            return BigDecimal.valueOf((productosStockBajo * 100.0) / totalProductos)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getPorcentajeAgotados() {
        if (totalProductos != null && totalProductos > 0 && productosAgotados != null) {
            return BigDecimal.valueOf((productosAgotados * 100.0) / totalProductos)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public String getResumenEstado() {
        if (productosAgotados != null && productosAgotados > 0) {
            return "CRÍTICO - Productos agotados: " + productosAgotados;
        } else if (productosStockBajo != null && productosStockBajo > 5) {
            return "ALERTA - Productos con stock bajo: " + productosStockBajo;
        } else {
            return "NORMAL - Inventario estable";
        }
    }
}