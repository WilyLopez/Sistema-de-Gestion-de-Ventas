package com.sgvi.sistema_ventas.model.enums;

/**
 * Estados de devoluciones de productos
 * Corresponde a la columna Estado en la tabla Devolucion
 */
public enum EstadoDevolucion {
    PENDIENTE("pendiente", "Pendiente", false, "Devolución solicitada, esperando revisión"),
    APROBADA("aprobada", "Aprobada", false, "Devolución aprobada, pendiente de completar"),
    RECHAZADA("rechazada", "Rechazada", true, "Devolución rechazada"),
    COMPLETADA("completada", "Completada", true, "Devolución completada y stock actualizado");

    private final String valor;
    private final String descripcion;
    private final boolean esFinal;
    private final String detalle;

    EstadoDevolucion(String valor, String descripcion, boolean esFinal, String detalle) {
        this.valor = valor;
        this.descripcion = descripcion;
        this.esFinal = esFinal;
        this.detalle = detalle;
    }

    public String getValor() {
        return valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isEsFinal() {
        return esFinal;
    }

    public String getDetalle() {
        return detalle;
    }

    public static EstadoDevolucion fromValor(String valor) {
        for (EstadoDevolucion estado : EstadoDevolucion.values()) {
            if (estado.valor.equalsIgnoreCase(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de devolución no válido: " + valor);
    }

    /**
     * Verifica si la devolución puede ser procesada
     */
    public boolean puedeProcesarse() {
        return this == APROBADA;
    }

    /**
     * Verifica si la devolución puede ser modificada
     */
    public boolean puedeModificarse() {
        return this == PENDIENTE;
    }

    /**
     * Verifica si actualiza el stock
     */
    public boolean actualizaStock() {
        return this == COMPLETADA;
    }

    /**
     * Obtiene la clase CSS para badge
     */
    public String getBadgeClass() {
        return switch (this) {
            case PENDIENTE -> "badge-warning";
            case APROBADA -> "badge-info";
            case RECHAZADA -> "badge-danger";
            case COMPLETADA -> "badge-success";
        };
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
