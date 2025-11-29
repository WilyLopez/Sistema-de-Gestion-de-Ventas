package com.sgvi.sistema_ventas.model.dto.common;

import java.time.LocalDateTime;

import com.sgvi.sistema_ventas.model.entity.AlertaStock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertaStockResponseDTO {
    private Long idAlerta;
    private String mensaje;
    private String tipoAlerta;
    private String nivelUrgencia;
    private Boolean leida;
    private LocalDateTime fechaAlerta;
    private LocalDateTime fechaLectura;
    private String nombreProducto;
    private Long idProducto;
    private String nombreUsuarioNotificado;
    private Integer stockActual;
    private Integer stockUmbral;
    private String accionTomada;

    public static AlertaStockResponseDTO fromEntity(AlertaStock alerta) {
        AlertaStockResponseDTO dto = new AlertaStockResponseDTO();
        dto.setIdAlerta(alerta.getIdAlerta());
        dto.setMensaje(alerta.getMensaje());
        dto.setTipoAlerta(alerta.getTipoAlerta().name());
        dto.setNivelUrgencia(alerta.getNivelUrgencia() != null ? alerta.getNivelUrgencia().name() : null);
        dto.setLeida(alerta.getLeida());
        dto.setFechaAlerta(alerta.getFechaAlerta());
        dto.setFechaLectura(alerta.getFechaLectura());
        dto.setStockActual(alerta.getStockActual());
        dto.setStockUmbral(alerta.getStockUmbral());
        dto.setAccionTomada(alerta.getAccionTomada());

        if (alerta.getProducto() != null) {
            dto.setNombreProducto(alerta.getProducto().getNombre());
            dto.setIdProducto(alerta.getProducto().getIdProducto());
        }

        if (alerta.getUsuarioNotificado() != null) {
            dto.setNombreUsuarioNotificado(alerta.getUsuarioNotificado().getUsername());
        }

        return dto;
    }
}
