package com.sgvi.sistema_ventas.model.dto.reporte;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO principal para el dashboard del sistema.
 * Contiene todos los datos necesarios para mostrar en el panel principal.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    // Período de análisis
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String periodoComparacion; // MES_ANTERIOR, AÑO_ANTERIOR, etc.

    // Métricas principales (KPI)
    private BigDecimal ventasHoy;
    private BigDecimal ventasMes;
    private Integer ventasCountHoy;
    private Integer ventasCountMes;
    private BigDecimal crecimientoVentas; // % vs período anterior

    private Integer nuevosClientesMes;
    private Integer clientesActivos;
    private BigDecimal crecimientoClientes;

    private BigDecimal valorInventario;
    private Integer productosStockBajo;
    private Integer productosAgotados;
    private Integer alertasPendientes;

    // Gráficos y tendencias
    private List<MetricaDiariaDTO> ventasUltimos7Dias;
    private List<MetricaCategoriaDTO> ventasPorCategoria;
    private List<MetricaProductoDTO> productosMasVendidos;
    private List<MetricaAlertaDTO> alertasRecientes;

    // Métricas de eficiencia
    private BigDecimal ticketPromedio;
    private BigDecimal conversionVentas; // % de visitas vs ventas
    private BigDecimal rentabilidadPromedio;
    private Integer devolucionesMes;
    private BigDecimal tasaDevolucion;

    // Estado del sistema
    private String estadoGeneral; // EXCELENTE, BUENO, REGULAR, CRÍTICO
    private Integer tareasPendientes;
    private Integer pedidosPendientes;
    private Integer reabastecimientosPendientes;

    // Métodos de utilidad
    public boolean tieneAlertasCriticas() {
        return alertasPendientes != null && alertasPendientes > 0;
    }

    public String getColorEstado() {
        if (estadoGeneral != null) {
            switch (estadoGeneral.toUpperCase()) {
                case "EXCELENTE": return "success";
                case "BUENO": return "info";
                case "REGULAR": return "warning";
                case "CRÍTICO": return "danger";
                default: return "secondary";
            }
        }
        return "secondary";
    }

    // Clases internas para estructuras complejas
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricaDiariaDTO {
        private LocalDate fecha;
        private BigDecimal ventas;
        private Integer cantidadVentas;
        private Integer productosVendidos;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricaCategoriaDTO {
        private String categoria;
        private BigDecimal ventas;
        private Integer cantidadVentas;
        private BigDecimal porcentaje;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricaProductoDTO {
        private String producto;
        private Integer cantidadVendida;
        private BigDecimal totalVentas;
        private String categoria;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricaAlertaDTO {
        private String tipoAlerta;
        private String producto;
        private String mensaje;
        private String nivelUrgencia;
        private String fecha;
    }
}