package com.sgvi.sistema_ventas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuración para tareas asíncronas y programadas.
 * Útil para generación de alertas automáticas y reportes.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    // Habilita @Async y @Scheduled en la aplicación
}
