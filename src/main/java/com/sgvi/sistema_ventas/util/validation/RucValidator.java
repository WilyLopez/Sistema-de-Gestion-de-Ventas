package com.sgvi.sistema_ventas.util.validation;

import org.springframework.stereotype.Component;

/**
 * Validador de RUC (Registro Único de Contribuyentes) peruano.
 * Valida formato y dígito verificador.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Component
public class RucValidator {

    /**
     * Valida un RUC peruano
     * @param ruc RUC a validar
     * @return true si es válido
     */
    public boolean validar(String ruc) {
        if (ruc == null || ruc.trim().isEmpty()) {
            return false;
        }

        // RUC debe tener exactamente 11 dígitos
        if (ruc.length() != 11) {
            return false;
        }

        // Solo debe contener números
        if (!ruc.matches("\\d+")) {
            return false;
        }

        // RUC debe empezar con 10, 15, 17 o 20
        String prefijo = ruc.substring(0, 2);
        if (!prefijo.equals("10") && !prefijo.equals("15") &&
                !prefijo.equals("17") && !prefijo.equals("20")) {
            return false;
        }

        // Validar dígito verificador
        return validarDigitoVerificador(ruc);
    }

    /**
     * Valida el dígito verificador del RUC usando el algoritmo oficial
     */
    private boolean validarDigitoVerificador(String ruc) {
        try {
            int[] factores = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
            int suma = 0;

            for (int i = 0; i < 10; i++) {
                int digito = Character.getNumericValue(ruc.charAt(i));
                suma += digito * factores[i];
            }

            int resto = suma % 11;
            int digitoVerificador = 11 - resto;

            if (digitoVerificador == 10) {
                digitoVerificador = 0;
            } else if (digitoVerificador == 11) {
                digitoVerificador = 1;
            }

            int ultimoDigito = Character.getNumericValue(ruc.charAt(10));
            return digitoVerificador == ultimoDigito;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Formatea el RUC con guiones para mejor legibilidad
     * @param ruc RUC sin formato
     * @return RUC formateado (ej: 20-12345678-9)
     */
    public String formatear(String ruc) {
        if (validar(ruc)) {
            return ruc.substring(0, 2) + "-" + ruc.substring(2, 10) + "-" + ruc.charAt(10);
        }
        return ruc;
    }
}