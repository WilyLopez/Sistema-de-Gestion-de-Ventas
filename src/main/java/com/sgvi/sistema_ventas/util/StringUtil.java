package com.sgvi.sistema_ventas.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Utilidades para manejo de cadenas de texto.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Component
public class StringUtil {

    /**
     * Capitaliza un texto (primera letra en mayúscula)
     * @param texto Texto a capitalizar
     * @return Texto capitalizado
     */
    public String capitalizar(String texto) {
        if (StringUtils.isBlank(texto)) {
            return texto;
        }
        return StringUtils.capitalize(texto.toLowerCase().trim());
    }

    /**
     * Capitaliza cada palabra de un texto
     * @param texto Texto a capitalizar
     * @return Texto con cada palabra capitalizada
     */
    public String capitalizarPalabras(String texto) {
        if (StringUtils.isBlank(texto)) {
            return texto;
        }

        String[] palabras = texto.trim().split("\\s+");
        StringBuilder resultado = new StringBuilder();

        for (String palabra : palabras) {
            if (resultado.length() > 0) {
                resultado.append(" ");
            }
            resultado.append(capitalizar(palabra));
        }

        return resultado.toString();
    }

    /**
     * Limpia un texto eliminando caracteres especiales
     * @param texto Texto a limpiar
     * @return Texto limpio
     */
    public String limpiar(String texto) {
        if (StringUtils.isBlank(texto)) {
            return "";
        }
        return texto.trim().replaceAll("\\s+", " ");
    }

    /**
     * Trunca un texto a una longitud máxima
     * @param texto Texto a truncar
     * @param longitudMaxima Longitud máxima
     * @return Texto truncado con "..." al final si se truncó
     */
    public String truncar(String texto, int longitudMaxima) {
        if (StringUtils.isBlank(texto) || texto.length() <= longitudMaxima) {
            return texto;
        }
        return texto.substring(0, longitudMaxima - 3) + "...";
    }

    /**
     * Convierte texto a formato slug (para URLs)
     * Ejemplo: "Hola Mundo" -> "hola-mundo"
     */
    public String toSlug(String texto) {
        if (StringUtils.isBlank(texto)) {
            return "";
        }

        return texto.toLowerCase()
                .replaceAll("[áàäâã]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöôõ]", "o")
                .replaceAll("[úùüû]", "u")
                .replaceAll("ñ", "n")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }

    /**
     * Enmascara un texto dejando solo los últimos N caracteres visibles
     * Útil para números de tarjeta, etc.
     */
    public String enmascarar(String texto, int caracteresVisibles) {
        if (StringUtils.isBlank(texto) || texto.length() <= caracteresVisibles) {
            return texto;
        }

        int caracteresOcultos = texto.length() - caracteresVisibles;
        return "*".repeat(caracteresOcultos) + texto.substring(caracteresOcultos);
    }
}
