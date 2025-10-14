package com.sgvi.sistema_ventas.model.enums;

/**
 * Tipos de alertas de stock
 * Corresponde a la columna TipoAlerta en la tabla AlertaStock
 */
public enum TipoAlerta {
    STOCK_MINIMO("stock_minimo", "Stock Mínimo", "Stock por debajo del mínimo establecido"),
    STOCK_AGOTADO("stock_agotado", "Stock Agotado", "Producto sin stock disponible"),
    STOCK_EXCESIVO("stock_excesivo", "Stock Excesivo", "Stock superior al recomendado"),
    REORDEN("reorden", "Reorden Necesario", "Se sugiere realizar pedido de reabastecimiento");

    private final String valor;
    private final String descripcion;
    private final String mensaje;

    TipoAlerta(String valor, String descripcion, String mensaje) {
        this.valor = valor;
        this.descripcion = descripcion;
        this.mensaje = mensaje;
    }

    public String getValor() {
        return valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public static TipoAlerta fromValor(String valor) {
        for (TipoAlerta tipo : TipoAlerta.values()) {
            if (tipo.valor.equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de alerta no válido: " + valor);
    }

    /**
     * Determina el nivel de urgencia por defecto según el tipo
     */
    public NivelUrgencia getNivelUrgenciaPorDefecto() {
        return switch (this) {
            case STOCK_AGOTADO -> NivelUrgencia.CRITICO;
            case STOCK_MINIMO, REORDEN -> NivelUrgencia.ALTO;
            case STOCK_EXCESIVO -> NivelUrgencia.BAJO;
        };
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
