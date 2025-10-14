package com.sgvi.sistema_ventas.model.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para encapsular la estructura del token JWT devuelto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtTokenDTO {

    /** El token de acceso JWT. */
    private String token;

    /** Tipo de token (siempre "Bearer"). */
    private String type = "Bearer";

    /** Tiempo de expiración del token en milisegundos. */
    private Long expiresIn;
}