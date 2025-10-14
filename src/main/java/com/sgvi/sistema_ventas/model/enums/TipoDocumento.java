package com.sgvi.sistema_ventas.model.enums;

/**
 * Tipos de documento de identidad válidos en el sistema
 * Corresponde a la columna TipoDocumento en la tabla Cliente
 */
public enum TipoDocumento {
    DNI("DNI", "Documento Nacional de Identidad", 8),
    RUC("RUC", "Registro Único de Contribuyentes", 11),
    CE("CE", "Carné de Extranjería", 12);

    private final String codigo;
    private final String descripcion;
    private final int longitudMaxima;

    TipoDocumento(String codigo, String descripcion, int longitudMaxima) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.longitudMaxima = longitudMaxima;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getLongitudMaxima() {
        return longitudMaxima;
    }

    /**
     * Obtiene el enum a partir del código
     */
    public static TipoDocumento fromCodigo(String codigo) {
        for (TipoDocumento tipo : TipoDocumento.values()) {
            if (tipo.codigo.equalsIgnoreCase(codigo)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de documento no válido: " + codigo);
    }

    /**
     * Valida si un número de documento es válido según su tipo
     */
    public boolean validarLongitud(String numeroDocumento) {
        if (numeroDocumento == null) {
            return false;
        }
        return numeroDocumento.length() == longitudMaxima;
    }
}
