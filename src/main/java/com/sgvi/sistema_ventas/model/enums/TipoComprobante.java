package com.sgvi.sistema_ventas.model.enums;

/**
 * Tipos de comprobante fiscal
 * Corresponde a las columnas TipoComprobante en Venta y Tipo en Comprobante
 */
public enum TipoComprobante {
    BOLETA("boleta", "Boleta de Venta", "B", false),
    FACTURA("factura", "Factura", "F", true);

    private final String valor;
    private final String descripcion;
    private final String serie;
    private final boolean requiereRuc;

    TipoComprobante(String valor, String descripcion, String serie, boolean requiereRuc) {
        this.valor = valor;
        this.descripcion = descripcion;
        this.serie = serie;
        this.requiereRuc = requiereRuc;
    }

    public String getValor() {
        return valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getSerie() {
        return serie;
    }

    public boolean isRequiereRuc() {
        return requiereRuc;
    }

    public static TipoComprobante fromValor(String valor) {
        for (TipoComprobante tipo : TipoComprobante.values()) {
            if (tipo.valor.equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de comprobante no válido: " + valor);
    }

    /**
     * Genera el formato completo de serie-número
     */
    public String formatearComprobante(String numero) {
        return String.format("%s001-%s", serie, numero);
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
