package com.sgvi.sistema_ventas.model.enums;

/**
 * Niveles de urgencia para alertas
 * Corresponde a la columna NivelUrgencia en la tabla AlertaStock
 */
public enum NivelUrgencia {
    BAJO("bajo", "Bajo", "#17a2b8", 1),
    MEDIO("medio", "Medio", "#ffc107", 2),
    ALTO("alto", "Alto", "#fd7e14", 3),
    CRITICO("critico", "Crítico", "#dc3545", 4);

    private final String valor;
    private final String descripcion;
    private final String colorHex;
    private final int prioridad; // Para ordenamiento

    NivelUrgencia(String valor, String descripcion, String colorHex, int prioridad) {
        this.valor = valor;
        this.descripcion = descripcion;
        this.colorHex = colorHex;
        this.prioridad = prioridad;
    }

    public String getValor() {
        return valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getColorHex() {
        return colorHex;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public static NivelUrgencia fromValor(String valor) {
        for (NivelUrgencia nivel : NivelUrgencia.values()) {
            if (nivel.valor.equalsIgnoreCase(valor)) {
                return nivel;
            }
        }
        throw new IllegalArgumentException("Nivel de urgencia no válido: " + valor);
    }

    /**
     * Obtiene la clase CSS de Bootstrap correspondiente
     */
    public String getBadgeClass() {
        return switch (this) {
            case BAJO -> "badge-info";
            case MEDIO -> "badge-warning";
            case ALTO -> "badge-orange";
            case CRITICO -> "badge-danger";
        };
    }

    /**
     * Determina el nivel de urgencia basado en stock actual vs mínimo
     */
    public static NivelUrgencia determinarPorStock(int stockActual, int stockMinimo) {
        if (stockActual == 0) {
            return CRITICO;
        } else if (stockActual <= 2) {
            return ALTO;
        } else if (stockActual <= stockMinimo) {
            return MEDIO;
        } else {
            return BAJO;
        }
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
