package com.sgvi.sistema_ventas.model.dto.venta;

import com.sgvi.sistema_ventas.model.enums.EstadoVenta;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para criterios de búsqueda y filtrado de ventas.
 * Se utiliza en endpoints de consulta con filtros.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaBusquedaDTO {

    private String codigoVenta;
    private Long idCliente;
    private String nombreCliente;
    private Long idUsuario;
    private EstadoVenta estado;
    private Long idMetodoPago;
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
    private BigDecimal totalMinimo;
    private BigDecimal totalMaximo;
    private String tipoComprobante;

    // Paginación
    private Integer pagina;
    private Integer tamanio;
    private String ordenarPor;
    private String direccion;

    // Métodos utilitarios para paginación
    public Integer getPagina() {
        return pagina != null ? pagina : 0;
    }

    public Integer getTamanio() {
        return tamanio != null ? tamanio : 20;
    }

    public String getOrdenarPor() {
        return ordenarPor != null ? ordenarPor : "fechaVenta";
    }

    public String getDireccion() {
        return direccion != null ? direccion : "DESC";
    }
}