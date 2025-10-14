package com.sgvi.sistema_ventas.model.dto.inventario;

import com.sgvi.sistema_ventas.model.enums.NivelUrgencia;
import com.sgvi.sistema_ventas.model.enums.TipoAlerta;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO para representar una alerta de stock.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaStockDTO {
    private Long idAlerta;
    private Long idProducto;
    private String codigoProducto;
    private String nombreProducto;
    private String marca;
    private String talla;
    private String color;
    private TipoAlerta tipoAlerta;
    private NivelUrgencia nivelUrgencia;
    private String mensaje;
    private Integer stockActual;
    private Integer stockUmbral;
    private Boolean leida;
    private LocalDateTime fechaAlerta;
    private LocalDateTime fechaLectura;
    private Long idUsuarioNotificado;
    private String nombreUsuarioNotificado;
    private String accionTomada;

    // Método para determinar si está pendiente
    public boolean isPendiente() {
        return !Boolean.TRUE.equals(leida);
    }

    // Método para obtener clase CSS
    public String getClaseCss() {
        return nivelUrgencia != null ? nivelUrgencia.getBadgeClass() : "badge-secondary";
    }
}