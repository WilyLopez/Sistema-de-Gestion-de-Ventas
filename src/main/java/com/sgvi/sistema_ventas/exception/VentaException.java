package com.sgvi.sistema_ventas.exception;

/**
 * Excepción lanzada cuando ocurre un error en operaciones de venta.
 * Mapea a HTTP 422 - Unprocessable Entity
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public class VentaException extends BusinessException {

    private Long idVenta;

    public VentaException(String mensaje) {
        super(mensaje);
    }

    public VentaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public VentaException(Long idVenta, String mensaje) {
        super(mensaje);
        this.idVenta = idVenta;
    }

    public Long getIdVenta() {
        return idVenta;
    }
}
