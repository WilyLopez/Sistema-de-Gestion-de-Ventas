package com.sgvi.sistema_ventas.exception;

/**
 * Excepción lanzada cuando no hay stock suficiente para una operación.
 * Mapea a HTTP 422 - Unprocessable Entity
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public class StockInsuficienteException extends BusinessException {

    private Long idProducto;
    private Integer stockActual;
    private Integer cantidadSolicitada;

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }

    public StockInsuficienteException(Long idProducto, Integer stockActual, Integer cantidadSolicitada) {
        super(String.format("Stock insuficiente. Disponible: %d, Solicitado: %d",
                stockActual, cantidadSolicitada));
        this.idProducto = idProducto;
        this.stockActual = stockActual;
        this.cantidadSolicitada = cantidadSolicitada;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public Integer getStockActual() {
        return stockActual;
    }

    public Integer getCantidadSolicitada() {
        return cantidadSolicitada;
    }
}

