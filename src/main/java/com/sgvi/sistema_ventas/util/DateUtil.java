package com.sgvi.sistema_ventas.util;

import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utilidades para manejo de fechas.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Component
public class DateUtil {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Formatea fecha a string
     * @param fecha Fecha a formatear
     * @return String en formato dd/MM/yyyy
     */
    public String formatearFecha(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.format(FORMATO_FECHA);
    }

    /**
     * Formatea fecha y hora a string
     * @param fechaHora Fecha y hora a formatear
     * @return String en formato dd/MM/yyyy HH:mm:ss
     */
    public String formatearFechaHora(LocalDateTime fechaHora) {
        if (fechaHora == null) {
            return "";
        }
        return fechaHora.format(FORMATO_FECHA_HORA);
    }

    /**
     * Obtiene fecha de inicio del día
     * @param fecha Fecha base
     * @return LocalDateTime a las 00:00:00
     */
    public LocalDateTime inicioDelDia(LocalDate fecha) {
        return fecha.atStartOfDay();
    }

    /**
     * Obtiene fecha de fin del día
     * @param fecha Fecha base
     * @return LocalDateTime a las 23:59:59
     */
    public LocalDateTime finDelDia(LocalDate fecha) {
        return fecha.atTime(23, 59, 59);
    }

    /**
     * Calcula días entre dos fechas
     * @param inicio Fecha inicio
     * @param fin Fecha fin
     * @return Número de días
     */
    public long diasEntre(LocalDate inicio, LocalDate fin) {
        return ChronoUnit.DAYS.between(inicio, fin);
    }

    /**
     * Verifica si una fecha está dentro de un rango
     * @param fecha Fecha a verificar
     * @param inicio Fecha inicio del rango
     * @param fin Fecha fin del rango
     * @return true si está dentro del rango
     */
    public boolean estaDentroDeRango(LocalDate fecha, LocalDate inicio, LocalDate fin) {
        return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
    }

    /**
     * Obtiene fecha de inicio del mes actual
     */
    public LocalDate inicioDelMes() {
        return LocalDate.now().withDayOfMonth(1);
    }

    /**
     * Obtiene fecha de fin del mes actual
     */
    public LocalDate finDelMes() {
        LocalDate hoy = LocalDate.now();
        return hoy.withDayOfMonth(hoy.lengthOfMonth());
    }

    /**
     * Verifica si una fecha ya pasó (es anterior a hoy)
     */
    public boolean esFechaPasada(LocalDate fecha) {
        return fecha.isBefore(LocalDate.now());
    }

    /**
     * Agrega días a una fecha
     */
    public LocalDate agregarDias(LocalDate fecha, int dias) {
        return fecha.plusDays(dias);
    }
}
