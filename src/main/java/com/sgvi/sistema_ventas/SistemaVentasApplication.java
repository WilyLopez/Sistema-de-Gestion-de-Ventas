package com.sgvi.sistema_ventas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.sgvi.sistema_ventas.repository")
public class SistemaVentasApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemaVentasApplication.class, args);
	}

}
