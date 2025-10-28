package com.sgvi.sistema_ventas.Service;

import com.sgvi.sistema_ventas.model.entity.Inventario;
import com.sgvi.sistema_ventas.model.entity.Producto;
import com.sgvi.sistema_ventas.model.enums.TipoMovimiento;
import com.sgvi.sistema_ventas.repository.InventarioRepository;
import com.sgvi.sistema_ventas.service.impl.InventarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el método registrarMovimiento() de InventarioServiceImpl.
 * Tema: tienda de ropa - control de inventarios y movimientos de stock.
 */
@ExtendWith(MockitoExtension.class)
public class InventarioServiceImplTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    private Inventario movimiento;

    @BeforeEach
    void setUp() {
        movimiento = new Inventario();
        movimiento.setIdMovimiento(1L);
        movimiento.setTipoMovimiento(TipoMovimiento.valueOf("ENTRADA"));
        movimiento.setStockAnterior(10);
        movimiento.setCantidad(5);
        movimiento.setStockNuevo(15);

        // ✅ Simulamos un producto existente para pasar la validación
        Producto producto = new Producto();
        producto.setIdProducto(1L);
        producto.setNombre("Polera de algodón");
        movimiento.setProducto(producto);
    }


    /**
     * Caso positivo:
     * Debe registrar correctamente un movimiento de inventario válido.
     */
    @Test
    void registrarMovimiento_DeberiaGuardarMovimientoSiEsValido() {
        // Simular repositorio guardando el movimiento
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> {
            Inventario inv = invocation.getArgument(0);
            inv.setIdMovimiento(1L);
            return inv;
        });

        Inventario resultado = inventarioService.registrarMovimiento(movimiento);

        assertNotNull(resultado, "El movimiento no debe ser nulo.");
        assertEquals(1L, resultado.getIdMovimiento(), "El ID del movimiento debe coincidir.");
        assertNotNull(resultado.getFechaMovimiento(), "La fecha de movimiento debe haberse asignado.");
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    /**
     * Caso negativo:
     * Debe lanzar excepción si el movimiento no es válido.
     */
    @Test
    void registrarMovimiento_DeberiaLanzarExcepcionSiMovimientoInvalido() {
        // Simular movimiento inválido
        Inventario movimientoInvalido = new Inventario();
        movimientoInvalido.setTipoMovimiento(null);

        // Sobrescribimos validarMovimiento() usando un espía
        InventarioServiceImpl spyService = spy(inventarioService);
        doThrow(new IllegalArgumentException("Movimiento inválido")).when(spyService).validarMovimiento(any(Inventario.class));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> spyService.registrarMovimiento(movimientoInvalido),
                "Se esperaba IllegalArgumentException si el movimiento no es válido."
        );

        assertTrue(ex.getMessage().contains("Movimiento inválido"));
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }
}
