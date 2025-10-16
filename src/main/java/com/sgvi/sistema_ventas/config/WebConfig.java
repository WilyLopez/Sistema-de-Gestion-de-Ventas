package com.sgvi.sistema_ventas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.format.DateTimeFormatter;

/**
 * Configuración web de la aplicación.
 * Configura CORS, formatters y otras opciones de MVC.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configuración de CORS para permitir peticiones desde el frontend
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:3000",      // React/Next.js
                        "http://localhost:4200",      // Angular
                        "http://localhost:8081"       // Otro puerto
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * Configuración de formatters para fechas
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setDateFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        registrar.setDateTimeFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        registrar.registerFormatters(registry);
    }
}
