package com.sgvi.sistema_ventas.exception;

/**
 * Excepción lanzada cuando se intenta crear un recurso que ya existe.
 * Mapea a HTTP 409 - Conflict
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String mensaje) {
        super(mensaje);
    }

    public DuplicateResourceException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public DuplicateResourceException(String recurso, String campo, Object valor) {
        super(String.format("%s ya existe con %s: '%s'", recurso, campo, valor));
    }
}
