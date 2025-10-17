package com.sgvi.sistema_ventas.util.validation;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

/**
 * Validador de correos electrónicos.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Component
public class EmailValidator {

    // Patrón regex para validación de email (RFC 5322)
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    /**
     * Valida un correo electrónico
     * @param email Email a validar
     * @return true si es válido
     */
    public boolean validar(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Normaliza el email (convierte a minúsculas y elimina espacios)
     * @param email Email a normalizar
     * @return Email normalizado
     */
    public String normalizar(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    /**
     * Extrae el dominio del email
     * @param email Email completo
     * @return Dominio del email
     */
    public String extraerDominio(String email) {
        if (validar(email)) {
            int indexArroba = email.indexOf('@');
            return email.substring(indexArroba + 1);
        }
        return null;
    }
}
