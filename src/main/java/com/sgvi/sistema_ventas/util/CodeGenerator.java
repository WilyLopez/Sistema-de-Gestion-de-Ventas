package com.sgvi.sistema_ventas.util;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

/**
 * Generador de códigos únicos para el sistema.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Component
public class CodeGenerator {

    private static final Random random = new Random();

    /**
     * Genera código de venta
     * Formato: V-YYYYMMDD-NNNNN
     * Ejemplo: V-20241016-00001
     */
    public String generarCodigoVenta(long count) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("V-%s-%05d", fecha, count);
    }

    /**
     * Genera código de pedido
     * Formato: PED-YYYYMMDD-NNNNN
     */
    public String generarCodigoPedido(long count) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("PED-%s-%05d", fecha, count);
    }

    /**
     * Genera código de pedido de reabastecimiento
     * Formato: REAB-YYYYMMDD-NNNNN
     */
    public String generarCodigoReabastecimiento(long count) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("REAB-%s-%05d", fecha, count);
    }

    /**
     * Genera código de producto aleatorio
     * Formato: PRO-XXXXXXXX
     */
    public String generarCodigoProducto() {
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "PRO-" + uuid;
    }

    /**
     * Genera código alfanumérico aleatorio
     * @param longitud Longitud del código
     * @return Código generado
     */
    public String generarCodigoAlfanumerico(int longitud) {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder codigo = new StringBuilder();

        for (int i = 0; i < longitud; i++) {
            int index = random.nextInt(caracteres.length());
            codigo.append(caracteres.charAt(index));
        }

        return codigo.toString();
    }

    /**
     * Genera serie de comprobante
     * @param tipo Tipo de comprobante (B=Boleta, F=Factura)
     * @return Serie generada (ej: B001, F001)
     */
    public String generarSerieComprobante(String tipo) {
        return tipo.toUpperCase() + "001";
    }

    /**
     * Genera número de comprobante
     * @param ultimoNumero Último número usado
     * @return Número generado en formato 00000001
     */
    public String generarNumeroComprobante(long ultimoNumero) {
        return String.format("%08d", ultimoNumero + 1);
    }
}
