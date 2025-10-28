package com.sgvi.sistema_ventas.model.dto.devolucion;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificarPlazoResponseDTO {

    private boolean dentroPlazo;
    private LocalDateTime fechaVenta;
    private LocalDateTime fechaLimite;
    private int diasRestantes;
    private String mensaje;
}