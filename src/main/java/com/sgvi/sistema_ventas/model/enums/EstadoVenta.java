package com.sgvi.sistema_ventas.model.enums;

/**
 * Estados posibles de una venta
 * Corresponde a la columna Estado en la tabla Venta
 */
public enum EstadoVenta {
    PENDIENTE("pendiente", "Pendiente", "warning"),
    PAGADO("pagado", "Pagado", "success"),
    ANULADO("anulado", "Anulado", "danger"),
    EN_PROCESO("en_proceso", "En Proceso", "info");

    private final String valor;
    private final String descripcion;
    private final String badge; // Para clases CSS de Bootstrap

    EstadoVenta(String valor, String descripcion, String badge) {
        this.valor = valor;
        this.descripcion = descripcion;
        this.badge = badge;
    }

    public String getValor() {
        return valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getBadge() {
        return badge;
    }

    public static EstadoVenta fromValor(String valor) {
        for (EstadoVenta estado : EstadoVenta.values()) {
            if (estado.valor.equalsIgnoreCase(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de venta no válido: " + valor);
    }

    /**
     * Verifica si la venta puede ser anulada
     */
    public boolean puedeAnularse() {
        return this == PAGADO;
    }

    /**
     * Verifica si la venta puede ser modificada
     */
    public boolean puedeModificarse() {
        return this == PENDIENTE || this == EN_PROCESO;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
