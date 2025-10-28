package com.sgvi.sistema_ventas.model.dto.devolucion;

import com.sgvi.sistema_ventas.model.enums.EstadoDevolucion;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevolucionResponseDTO {

    private Long idDevolucion;
    private Long idVenta;
    private Long idUsuario;
    private String nombreUsuario;
    private LocalDateTime fechaDevolucion;
    private String motivo;
    private EstadoDevolucion estado;
    private String estadoDescripcion;
    private BigDecimal montoDevolucion;
    private List<DetalleDevolucionResponseDTO> detalles;
    private boolean dentroPlazo;
    private String badgeClass;
}