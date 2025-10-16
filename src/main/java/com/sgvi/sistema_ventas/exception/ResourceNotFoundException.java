package com.sgvi.sistema_ventas.exception;

/**
 * Excepción lanzada cuando un recurso solicitado no existe en el sistema.
 * Mapea a HTTP 404 - Not Found
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }

    public ResourceNotFoundException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public ResourceNotFoundException(String recurso, String campo, Object valor) {
        super(String.format("%s no encontrado con %s: '%s'", recurso, campo, valor));
    }
}
