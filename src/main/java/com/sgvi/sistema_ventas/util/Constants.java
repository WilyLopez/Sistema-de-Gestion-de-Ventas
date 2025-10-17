package com.sgvi.sistema_ventas.util;

import java.math.BigDecimal;

/**
 * Constantes del sistema.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
public final class Constants {

    private Constants() {
        // Clase de constantes, no debe instanciarse
    }

    // ========== IMPUESTOS ==========
    public static final BigDecimal IGV_RATE = BigDecimal.valueOf(0.18); // 18%

    // ========== STOCK ==========
    public static final int STOCK_MINIMO_DEFAULT = 5;
    public static final int STOCK_CRITICO = 2;

    // ========== PLAZOS ==========
    public static final int DIAS_PLAZO_DEVOLUCION = 30;
    public static final int HORAS_PLAZO_ANULACION_VENTA = 24;

    // ========== PAGINACIÓN ==========
    public static final int PAGE_SIZE_DEFAULT = 20;
    public static final int PAGE_SIZE_MAX = 100;

    // ========== FORMATOS ==========
    public static final String FORMATO_FECHA = "dd/MM/yyyy";
    public static final String FORMATO_FECHA_HORA = "dd/MM/yyyy HH:mm:ss";
    public static final String FORMATO_MONEDA = "S/ %.2f";

    // ========== ROLES ==========
    public static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";
    public static final String ROL_VENDEDOR = "VENDEDOR";
    public static final String ROL_EMPLEADO = "EMPLEADO";

    // ========== MENSAJES ==========
    public static final String MSG_RECURSO_NO_ENCONTRADO = "Recurso no encontrado";
    public static final String MSG_OPERACION_EXITOSA = "Operación realizada exitosamente";
    public static final String MSG_ERROR_INESPERADO = "Ha ocurrido un error inesperado";

    // ========== CÓDIGOS DE ERROR ==========
    public static final String ERR_STOCK_INSUFICIENTE = "ERR_STOCK_001";
    public static final String ERR_VENTA_NO_ANULABLE = "ERR_VENTA_001";
    public static final String ERR_DUPLICADO = "ERR_DUP_001";
}
