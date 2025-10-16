package com.sgvi.sistema_ventas.exception;

/**
 * Excepción lanzada cuando la validación de datos falla.
 * Mapea a HTTP 400 - Bad Request
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public class ValidationException extends RuntimeException {

    private String campo;

    public ValidationException(String mensaje) {
        super(mensaje);
    }

    public ValidationException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public ValidationException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
