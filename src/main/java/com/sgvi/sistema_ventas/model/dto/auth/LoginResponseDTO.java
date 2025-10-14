package com.sgvi.sistema_ventas.model.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.sgvi.sistema_ventas.model.dto.usuario.UsuarioDTO;

/**
 * DTO para la respuesta después de un login exitoso.
 * Combina el token JWT con la información principal del usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    /** El objeto que contiene el token JWT. */
    private JwtTokenDTO tokenInfo;

    /** Información esencial del usuario que ha iniciado sesión. */
    private UsuarioDTO userInfo;
}