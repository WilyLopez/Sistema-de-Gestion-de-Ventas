package com.sgvi.sistema_ventas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.client.RestTemplate;

/**
 * Configuraciones generales de la aplicación.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Configuration
public class AppConfig {

    /**
     * Bean para mensajes internacionalizados
     */
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages/messages");
        source.setDefaultEncoding("UTF-8");
        return source;
    }

    /**
     * Bean para realizar llamadas HTTP (si se necesitan integraciones externas)
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
