package com.sgvi.sistema_ventas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración de JPA y auditoría.
 * Habilita auditoría automática de entidades.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.sgvia.sistema.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
    // La configuración está habilitada mediante anotaciones
}
