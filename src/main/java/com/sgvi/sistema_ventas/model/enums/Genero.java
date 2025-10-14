package com.sgvi.sistema_ventas.model.enums;

/**
 * Géneros para productos de ropa
 * Corresponde a la columna Genero en la tabla Producto
 */
public enum Genero {
    HOMBRE("hombre", "Hombre"),
    MUJER("mujer", "Mujer"),
    UNISEX("unisex", "Unisex"),
    NINO("niño", "Niño"),
    NINA("niña", "Niña");

    private final String valor;
    private final String descripcion;

    Genero(String valor, String descripcion) {
        this.valor = valor;
        this.descripcion = descripcion;
    }

    public String getValor() {
        return valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Obtiene el enum a partir del valor en base de datos
     */
    public static Genero fromValor(String valor) {
        for (Genero genero : Genero.values()) {
            if (genero.valor.equalsIgnoreCase(valor)) {
                return genero;
            }
        }
        throw new IllegalArgumentException("Género no válido: " + valor);
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
