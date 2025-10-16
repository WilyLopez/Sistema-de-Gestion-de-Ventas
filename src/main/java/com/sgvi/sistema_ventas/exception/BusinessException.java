package com.sgvi.sistema_ventas.exception;

/**
 * Excepción lanzada cuando se viola una regla de negocio.
 * Mapea a HTTP 422 - Unprocessable Entity
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public class BusinessException extends RuntimeException {

    private String codigoError;

    public BusinessException(String mensaje) {
        super(mensaje);
    }

    public BusinessException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public BusinessException(String codigoError, String mensaje) {
        super(mensaje);
        this.codigoError = codigoError;
    }

    public String getCodigoError() {
        return codigoError;
    }
}
