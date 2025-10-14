package com.sgvi.sistema_ventas.model.enums;

/**
 * Tipos de movimiento de inventario
 * Corresponde a la columna TipoMovimiento en la tabla Inventario
 */
public enum TipoMovimiento {
    ENTRADA("entrada", "Entrada", true, "Ingreso de mercadería"),
    SALIDA("salida", "Salida", false, "Venta o salida de producto"),
    AJUSTE("ajuste", "Ajuste", null, "Ajuste de inventario (puede ser + o -)"),
    DEVOLUCION("devolucion", "Devolución", true, "Devolución de producto");

    private final String valor;
    private final String descripcion;
    private final Boolean incrementaStock; // null = puede ser ambos
    private final String detalle;

    TipoMovimiento(String valor, String descripcion, Boolean incrementaStock, String detalle) {
        this.valor = valor;
        this.descripcion = descripcion;
        this.incrementaStock = incrementaStock;
        this.detalle = detalle;
    }

    public String getValor() {
        return valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Boolean getIncrementaStock() {
        return incrementaStock;
    }

    public String getDetalle() {
        return detalle;
    }

    public static TipoMovimiento fromValor(String valor) {
        for (TipoMovimiento tipo : TipoMovimiento.values()) {
            if (tipo.valor.equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de movimiento no válido: " + valor);
    }

    /**
     * Calcula el nuevo stock basado en el tipo de movimiento
     */
    public int calcularNuevoStock(int stockActual, int cantidad) {
        return switch (this) {
            case ENTRADA, DEVOLUCION -> stockActual + cantidad;
            case SALIDA -> stockActual - cantidad;
            case AJUSTE -> cantidad; // En ajuste, la cantidad ES el nuevo stock
        };
    }

    /**
     * Determina si el movimiento requiere autorización especial
     */
    public boolean requiereAutorizacion() {
        return this == AJUSTE;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
