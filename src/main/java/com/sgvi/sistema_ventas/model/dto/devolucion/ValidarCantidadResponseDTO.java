package com.sgvi.sistema_ventas.model.dto.devolucion;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidarCantidadResponseDTO {

    private boolean cantidadValida;
    private Integer cantidadVendida;
    private Integer cantidadYaDevuelta;
    private Integer cantidadDisponible;
    private String mensaje;
}