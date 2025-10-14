package com.sgvi.sistema_ventas.model.dto.reporte;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para reportes de devoluciones y análisis de productos devueltos.
 * Ayuda a identificar problemas de calidad o insatisfacción del cliente.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDevolucionDTO {

    // Filtros
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long idProducto;
    private Long idUsuario;
    private String motivo;

    // Estadísticas de devoluciones
    private Integer totalDevoluciones;
    private BigDecimal montoTotalDevoluciones;
    private BigDecimal porcentajeDevoluciones; // % sobre ventas totales
    private Integer productosDevueltos;

    // Análisis por motivo
    private String motivoPrincipal;
    private Integer devolucionesPorCalidad;
    private Integer devolucionesPorTalla;
    private Integer devolucionesPorColor;
    private Integer devolucionesPorError;
    private Integer devolucionesOtros;

    // Productos más devueltos
    private String productoMasDevuelto;
    private Integer vecesDevuelto;
    private BigDecimal tasaDevolucionProducto; // % de devolución del producto

    // Métodos de análisis
    public BigDecimal getTasaDevolucionGeneral() {
        if (totalDevoluciones != null && totalDevoluciones > 0) {
            return BigDecimal.valueOf(porcentajeDevoluciones != null ?
                    porcentajeDevoluciones.doubleValue() : 0.0);
        }
        return BigDecimal.ZERO;
    }

    public String getNivelRiesgo() {
        BigDecimal tasa = getTasaDevolucionGeneral();
        if (tasa.compareTo(BigDecimal.valueOf(10)) > 0) {
            return "ALTO";
        } else if (tasa.compareTo(BigDecimal.valueOf(5)) > 0) {
            return "MEDIO";
        } else {
            return "BAJO";
        }
    }

    public String getRecomendacion() {
        String nivel = getNivelRiesgo();
        switch (nivel) {
            case "ALTO":
                return "Revisar urgentemente calidad de productos y procesos";
            case "MEDIO":
                return "Monitorear productos problemáticos y mejorar controles";
            case "BAJO":
                return "Nivel aceptable, mantener seguimiento";
            default:
                return "No hay datos suficientes";
        }
    }
}