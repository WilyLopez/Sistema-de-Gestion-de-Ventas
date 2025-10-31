package com.sgvi.sistema_ventas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SistemaVentasApplicationTests {

	@Test
	void contextLoads() {
        System.out.println("El contexto de Spring se cargó correctamente");
        assertTrue(true, "El test pasó exitosamente");
	}

}
