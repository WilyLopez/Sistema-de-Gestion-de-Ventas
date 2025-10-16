package com.sgvi.sistema_ventas.exception;

/**
 * Excepción lanzada cuando la autenticación falla o no tiene permisos.
 * Mapea a HTTP 401 - Unauthorized
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String mensaje) {
        super(mensaje);
    }

    public UnauthorizedException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
