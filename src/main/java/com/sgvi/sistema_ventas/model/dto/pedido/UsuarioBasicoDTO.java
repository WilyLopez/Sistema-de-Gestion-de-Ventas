package com.sgvi.sistema_ventas.model.dto.pedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO básico de usuario para incluir en respuestas de pedido
 *
 * @author Wilian Lopez
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioBasicoDTO {

    private Long idUsuario;
    private String username;
    private String nombreCompleto;
}