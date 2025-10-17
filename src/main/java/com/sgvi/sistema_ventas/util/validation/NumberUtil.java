package com.sgvi.sistema_ventas.util.validation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utilidades para manejo de números y cálculos.
 * Formateo de moneda, porcentajes y operaciones matemáticas.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Component
public class NumberUtil {

    private static final Locale LOCALE_PERU = new Locale("es", "PE");
    private static final int DECIMALES_MONEDA = 2;
    private static final int DECIMALES_PORCENTAJE = 2;

    /**
     * Formatea un número como moneda peruana (S/. 1,234.56)
     * @param valor Valor a formatear
     * @return String formateado como moneda
     */
    public String formatearMoneda(BigDecimal valor) {
        if (valor == null) {
            return "S/. 0.00";
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(LOCALE_PERU);
        formatter.setMinimumFractionDigits(DECIMALES_MONEDA);
        formatter.setMaximumFractionDigits(DECIMALES_MONEDA);

        return formatter.format(valor);
    }

    /**
     * Formatea un número como moneda peruana (sobrecarga para Double)
     */
    public String formatearMoneda(Double valor) {
        return formatearMoneda(valor != null ? BigDecimal.valueOf(valor) : BigDecimal.ZERO);
    }

    /**
     * Formatea un número como porcentaje (12.5%)
     * @param valor Valor decimal (ej: 0.125 para 12.5%)
     * @return String formateado como porcentaje
     */
    public String formatearPorcentaje(BigDecimal valor) {
        if (valor == null) {
            return "0.00%";
        }

        BigDecimal porcentaje = valor.multiply(BigDecimal.valueOf(100))
                .setScale(DECIMALES_PORCENTAJE, RoundingMode.HALF_UP);

        return porcentaje + "%";
    }

    /**
     * Formatea un número con separadores de miles (1,234.56)
     */
    public String formatearNumero(BigDecimal valor) {
        if (valor == null) {
            return "0.00";
        }

        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        return formatter.format(valor);
    }

    /**
     * Formatea un número entero con separadores de miles (1,234)
     */
    public String formatearEntero(Integer valor) {
        if (valor == null) {
            return "0";
        }

        DecimalFormat formatter = new DecimalFormat("#,##0");
        return formatter.format(valor);
    }

    /**
     * Redondea un BigDecimal a N decimales
     * @param valor Valor a redondear
     * @param decimales Número de decimales
     * @return Valor redondeado
     */
    public BigDecimal redondear(BigDecimal valor, int decimales) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }

        return valor.setScale(decimales, RoundingMode.HALF_UP);
    }

    /**
     * Redondea a 2 decimales (para moneda)
     */
    public BigDecimal redondearMoneda(BigDecimal valor) {
        return redondear(valor, DECIMALES_MONEDA);
    }

    /**
     * Calcula porcentaje de un valor
     * @param valor Valor base
     * @param porcentaje Porcentaje a calcular (ej: 18 para 18%)
     * @return Resultado del cálculo
     */
    public BigDecimal calcularPorcentaje(BigDecimal valor, BigDecimal porcentaje) {
        if (valor == null || porcentaje == null) {
            return BigDecimal.ZERO;
        }

        return valor.multiply(porcentaje)
                .divide(BigDecimal.valueOf(100), DECIMALES_MONEDA, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el IGV (18%) de un monto
     * @param subtotal Subtotal sin IGV
     * @return Monto del IGV
     */
    public BigDecimal calcularIGV(BigDecimal subtotal) {
        return calcularPorcentaje(subtotal, BigDecimal.valueOf(18));
    }

    /**
     * Calcula el total con IGV
     * @param subtotal Subtotal sin IGV
     * @return Total con IGV incluido
     */
    public BigDecimal calcularTotalConIGV(BigDecimal subtotal) {
        BigDecimal igv = calcularIGV(subtotal);
        return subtotal.add(igv);
    }

    /**
     * Convierte String a BigDecimal de forma segura
     * @param valor String a convertir
     * @return BigDecimal o ZERO si falla
     */
    public BigDecimal parseBigDecimal(String valor) {
        try {
            if (valor == null || valor.trim().isEmpty()) {
                return BigDecimal.ZERO;
            }

            // Remover símbolos de moneda y espacios
            String valorLimpio = valor.replaceAll("[^0-9.-]", "");
            return new BigDecimal(valorLimpio);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Compara dos BigDecimal de forma segura
     * @return true si son iguales (considerando null como 0)
     */
    public boolean sonIguales(BigDecimal a, BigDecimal b) {
        BigDecimal valorA = a != null ? a : BigDecimal.ZERO;
        BigDecimal valorB = b != null ? b : BigDecimal.ZERO;

        return valorA.compareTo(valorB) == 0;
    }

    /**
     * Verifica si un valor es mayor que otro
     */
    public boolean esMayor(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return false;
        }
        return a.compareTo(b) > 0;
    }

    /**
     * Verifica si un valor es menor que otro
     */
    public boolean esMenor(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return false;
        }
        return a.compareTo(b) < 0;
    }

    /**
     * Suma segura de BigDecimal (maneja nulls)
     */
    public BigDecimal sumar(BigDecimal... valores) {
        BigDecimal suma = BigDecimal.ZERO;

        for (BigDecimal valor : valores) {
            if (valor != null) {
                suma = suma.add(valor);
            }
        }

        return suma;
    }

    /**
     * Promedio de una lista de valores
     */
    public BigDecimal promedio(BigDecimal... valores) {
        if (valores == null || valores.length == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal suma = sumar(valores);
        return suma.divide(BigDecimal.valueOf(valores.length), DECIMALES_MONEDA, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el margen de ganancia en porcentaje
     * @param precioVenta Precio de venta
     * @param precioCompra Precio de compra
     * @return Margen de ganancia en porcentaje
     */
    public BigDecimal calcularMargenGanancia(BigDecimal precioVenta, BigDecimal precioCompra) {
        if (precioCompra == null || precioCompra.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal ganancia = precioVenta.subtract(precioCompra);
        BigDecimal margen = ganancia.divide(precioCompra, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return margen.setScale(DECIMALES_PORCENTAJE, RoundingMode.HALF_UP);
    }

    /**
     * Valida que un número esté dentro de un rango
     */
    public boolean estaEnRango(BigDecimal valor, BigDecimal minimo, BigDecimal maximo) {
        if (valor == null) {
            return false;
        }

        boolean mayorQueMinimo = minimo == null || valor.compareTo(minimo) >= 0;
        boolean menorQueMaximo = maximo == null || valor.compareTo(maximo) <= 0;

        return mayorQueMinimo && menorQueMaximo;
    }

    /**
     * Formatea número de unidades con texto
     * @param cantidad Cantidad
     * @param singular Texto en singular
     * @param plural Texto en plural
     * @return String formateado (ej: "1 producto", "5 productos")
     */
    public String formatearCantidad(Integer cantidad, String singular, String plural) {
        if (cantidad == null || cantidad == 0) {
            return "0 " + plural;
        }

        return cantidad + " " + (cantidad == 1 ? singular : plural);
    }
}
