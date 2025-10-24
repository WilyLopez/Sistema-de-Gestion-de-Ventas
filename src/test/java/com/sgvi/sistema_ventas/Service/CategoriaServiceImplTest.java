package com.sgvi.sistema_ventas.Service;

import com.sgvi.sistema_ventas.exception.DuplicateResourceException;
import com.sgvi.sistema_ventas.model.entity.Categoria;
import com.sgvi.sistema_ventas.repository.CategoriaRepository;
import com.sgvi.sistema_ventas.service.impl.CategoriaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el método crear() de CategoriaServiceImpl.
 * Tema: tienda de ropa.
 *
 * Este test verifica que una nueva categoría de ropa se crea correctamente,
 * se validan duplicados y se asignan los valores automáticos esperados.
 */
@ExtendWith(MockitoExtension.class)
public class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    private Categoria categoriaRopa;

    @BeforeEach
    void setUp() {
        categoriaRopa = new Categoria();
        categoriaRopa.setNombre("Ropa Deportiva");
        categoriaRopa.setDescripcion("Prendas cómodas y modernas para entrenar o vestir casualmente.");
    }

    /**
     * Caso positivo:
     * Debe crear una categoría de ropa correctamente cuando el nombre no está duplicado.
     */
    @Test
    void crearCategoria_DeberiaGuardarCorrectamente() {
        // Configurar el mock: el nombre no existe
        when(categoriaRepository.existsByNombre("Ropa Deportiva")).thenReturn(false);

        // Simular que el repositorio devuelve la categoría guardada
        Categoria categoriaGuardada = new Categoria();
        categoriaGuardada.setIdCategoria(1L);
        categoriaGuardada.setNombre("Ropa Deportiva");
        categoriaGuardada.setDescripcion("Prendas cómodas y modernas para entrenar o vestir casualmente.");
        categoriaGuardada.setEstado(true);
        categoriaGuardada.setFechaCreacion(LocalDateTime.now());
        categoriaGuardada.setFechaActualizacion(LocalDateTime.now());

        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaGuardada);

        // Ejecutar el método
        Categoria resultado = categoriaService.crear(categoriaRopa);

        // Validaciones
        assertNotNull(resultado);
        assertEquals("Ropa Deportiva", resultado.getNombre());
        assertTrue(resultado.getEstado(), "El estado de la categoría debería ser TRUE al crearse.");
        assertNotNull(resultado.getFechaCreacion());
        assertNotNull(resultado.getFechaActualizacion());
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
    }

    /**
     * Caso negativo:
     * Debe lanzar una excepción si ya existe una categoría con el mismo nombre.
     */
    @Test
    void crearCategoria_DeberiaLanzarExcepcionSiDuplicada() {
        // Configurar el mock: el nombre ya existe
        when(categoriaRepository.existsByNombre("Ropa Deportiva")).thenReturn(true);

        // Ejecutar y verificar excepción
        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> categoriaService.crear(categoriaRopa),
                "Se esperaba una DuplicateResourceException si la categoría ya existe"
        );

        assertTrue(ex.getMessage().contains("ya existe"));
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }
}