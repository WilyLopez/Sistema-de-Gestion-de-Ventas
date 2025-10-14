package com.sgvi.sistema_ventas.model.dto.reporte;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para reportes de ventas con datos agregados.
 * Utilizado para generar reportes estadísticos de ventas.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteVentaDTO {

    // Filtros de búsqueda
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long idUsuario;
    private Long idCategoria;
    private String agrupacion; // DIA, SEMANA, MES, CATEGORIA, PRODUCTO

    // Datos agregados
    private String periodo;
    private String categoria;
    private String producto;
    private String usuario;
    private Integer cantidadVentas;
    private BigDecimal totalVentas;
    private BigDecimal promedioVenta;
    private BigDecimal ventaMaxima;
    private BigDecimal ventaMinima;
    private Integer cantidadProductosVendidos;
    private BigDecimal porcentajeCrecimiento;

    // Métodos utilitarios
    public String getFormatoPeriodo() {
        if (periodo != null && agrupacion != null) {
            switch (agrupacion.toUpperCase()) {
                case "DIA":
                    return "Día: " + periodo;
                case "SEMANA":
                    return "Semana: " + periodo;
                case "MES":
                    return "Mes: " + periodo;
                case "CATEGORIA":
                    return "Categoría: " + periodo;
                case "PRODUCTO":
                    return "Producto: " + periodo;
                default:
                    return periodo;
            }
        }
        return periodo;
    }

    public BigDecimal getTicketPromedio() {
        if (cantidadVentas != null && cantidadVentas > 0 && totalVentas != null) {
            return totalVentas.divide(BigDecimal.valueOf(cantidadVentas), 2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}