package com.sgvi.sistema_ventas.model.dto.pedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO básico de cliente para incluir en respuestas de pedido
 *
 * @author Wilian Lopez
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteBasicoDTO {

    private Long idCliente;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombreCompleto;
    private String telefono;
    private String correo;
}