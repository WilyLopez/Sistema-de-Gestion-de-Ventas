package com.sgvi.sistema_ventas.Service;

import com.sgvi.sistema_ventas.model.enums.EstadoDevolucion;
import com.sgvi.sistema_ventas.exception.ValidationException;
import com.sgvi.sistema_ventas.exception.VentaException;
import com.sgvi.sistema_ventas.model.entity.*;
import com.sgvi.sistema_ventas.repository.DevolucionRepository;
import com.sgvi.sistema_ventas.repository.DetalleDevolucionRepository;
import com.sgvi.sistema_ventas.service.impl.DevolucionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el método crearDevolucion() de DevolucionServiceImpl.
 * Tema: tienda de ropa - gestión de devoluciones.
 *
 * Este test verifica:
 *  - Que se cree correctamente una devolución válida.
 *  - Que se lance una excepción si la venta está fuera del plazo.
 *  - Que se lance una excepción si hay una cantidad inválida en un detalle.
 */
@ExtendWith(MockitoExtension.class)
public class DevolucionServiceImplTest {

    @Mock
    private DevolucionRepository devolucionRepository;

    @Mock
    private DetalleDevolucionRepository detalleDevolucionRepository;

    @Spy
    @InjectMocks
    private DevolucionServiceImpl devolucionService;

    private Devolucion devolucion;
    private DetalleDevolucion detalle;

    @BeforeEach
    void setUp() {
        // Simular una venta y producto
        Venta venta = new Venta();
        venta.setIdVenta(10L);

        Producto producto = new Producto();
        producto.setIdProducto(5L);

        // Crear devolución base
        devolucion = new Devolucion();
        devolucion.setVenta(venta);

        // Crear detalle base
        detalle = new DetalleDevolucion();
        detalle.setProducto(producto);
        detalle.setCantidad(2);
    }

    /**
     * Caso positivo:
     * Debe crear correctamente una devolución válida.
     */
    @Test
    void crearDevolucion_DeberiaGuardarCorrectamente() {
        // Configurar mocks
        doReturn(true).when(devolucionService).estaDentroPlazo(10L);
        doReturn(true).when(devolucionService).validarCantidadDevolucion(10L, 5L, 2);

        Devolucion devolucionGuardada = new Devolucion();
        devolucionGuardada.setIdDevolucion(1L);
        devolucionGuardada.setVenta(devolucion.getVenta());
        devolucionGuardada.setEstado(EstadoDevolucion.PENDIENTE);
        devolucionGuardada.setFechaDevolucion(LocalDateTime.now());

        when(devolucionRepository.save(any(Devolucion.class))).thenReturn(devolucionGuardada);

        // Ejecutar método
        Devolucion resultado = devolucionService.crearDevolucion(devolucion, List.of(detalle));

        // Verificar resultados
        assertNotNull(resultado);
        assertEquals(EstadoDevolucion.PENDIENTE, resultado.getEstado());
        verify(devolucionRepository, atLeastOnce()).save(any(Devolucion.class));
        verify(detalleDevolucionRepository, times(1)).save(any(DetalleDevolucion.class));
    }

    /**
     * Caso negativo 1:
     * Debe lanzar excepción si la venta está fuera del plazo.
     */
    @Test
    void crearDevolucion_DeberiaLanzarExcepcionSiFueraDePlazo() {
        doReturn(false).when(devolucionService).estaDentroPlazo(10L);

        VentaException ex = assertThrows(
                VentaException.class,
                () -> devolucionService.crearDevolucion(devolucion, List.of(detalle)),
                "Se esperaba una VentaException si la venta está fuera del plazo"
        );

        assertTrue(ex.getMessage().contains("fuera del plazo"));
        verify(devolucionRepository, never()).save(any(Devolucion.class));
    }

    /**
     * Caso negativo 2:
     * Debe lanzar excepción si una cantidad de devolución es inválida.
     */
    @Test
    void crearDevolucion_DeberiaLanzarExcepcionSiCantidadInvalida() {
        doReturn(true).when(devolucionService).estaDentroPlazo(10L);
        doReturn(false).when(devolucionService).validarCantidadDevolucion(10L, 5L, 2);

        jakarta.validation.ValidationException ex = assertThrows(
                jakarta.validation.ValidationException.class,
                () -> devolucionService.crearDevolucion(devolucion, List.of(detalle)),
                "Se esperaba una ValidationException si la cantidad es inválida"
        );


        assertTrue(ex.getMessage().contains("Cantidad de devolución inválida"));
        verify(detalleDevolucionRepository, never()).save(any(DetalleDevolucion.class));
    }
}
