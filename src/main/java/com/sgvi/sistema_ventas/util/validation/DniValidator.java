package com.sgvi.sistema_ventas.util.validation;

import org.springframework.stereotype.Component;

/**
 * Validador de DNI (Documento Nacional de Identidad) peruano.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Component
public class DniValidator {

    /**
     * Valida un DNI peruano
     * @param dni DNI a validar
     * @return true si es válido
     */
    public boolean validar(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            return false;
        }

        // DNI debe tener exactamente 8 dígitos
        if (dni.length() != 8) {
            return false;
        }

        // Solo debe contener números
        if (!dni.matches("\\d+")) {
            return false;
        }

        // No puede ser 00000000
        if (dni.equals("00000000")) {
            return false;
        }

        // No puede empezar con 0
        if (dni.startsWith("0")) {
            return false;
        }

        return true;
    }

    /**
     * Formatea el DNI con espacios para mejor legibilidad
     * @param dni DNI sin formato
     * @return DNI formateado (ej: 1234 5678)
     */
    public String formatear(String dni) {
        if (validar(dni)) {
            return dni.substring(0, 4) + " " + dni.substring(4);
        }
        return dni;
    }
}
