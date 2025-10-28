package com.sgvi.sistema_ventas.Service;

import com.sgvi.sistema_ventas.exception.DuplicateResourceException;
import com.sgvi.sistema_ventas.exception.ResourceNotFoundException;
import com.sgvi.sistema_ventas.model.entity.Cliente;
import com.sgvi.sistema_ventas.model.enums.TipoDocumento;
import com.sgvi.sistema_ventas.repository.ClienteRepository;
import com.sgvi.sistema_ventas.service.impl.ClienteServiceImpl;
import com.sgvi.sistema_ventas.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el método actualizar() de ClienteServiceImpl.
 * Tema: tienda de ropa.
 *
 * Verifica el comportamiento ante casos normales y errores al actualizar clientes.
 */
@ExtendWith(MockitoExtension.class)
public class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private Cliente clienteExistente;
    private Cliente clienteNuevo;

    @BeforeEach
    void setUp() {
        clienteExistente = new Cliente();
        clienteExistente.setIdCliente(1L);
        clienteExistente.setTipoDocumento(TipoDocumento.valueOf("DNI"));
        clienteExistente.setNumeroDocumento("12345678");
        clienteExistente.setNombre("Juan");
        clienteExistente.setApellido("Pérez");
        clienteExistente.setCorreo("juan@correo.com");
        clienteExistente.setTelefono("999999999");
        clienteExistente.setDireccion("Av. Siempre Viva 123");
        clienteExistente.setFechaNacimiento(LocalDate.of(1990, 5, 10));

        clienteNuevo = new Cliente();
        clienteNuevo.setTipoDocumento(TipoDocumento.valueOf("DNI"));
        clienteNuevo.setNumeroDocumento("12345678");
        clienteNuevo.setNombre("Juan");
        clienteNuevo.setApellido("Pérez");
        clienteNuevo.setCorreo("nuevo@correo.com");
        clienteNuevo.setTelefono("999999999");
        clienteNuevo.setDireccion("Av. Siempre Viva 123");
        clienteNuevo.setFechaNacimiento(LocalDate.of(1990, 5, 10));
    }

    /**
     * Caso negativo:
     * Debe lanzar una excepción si el nuevo correo ya existe en otro cliente.
     */
    @Test
    void actualizarCliente_DeberiaLanzarExcepcionSiCorreoDuplicado() {
        // Configurar mocks
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.existsByCorreo("nuevo@correo.com")).thenReturn(true);

        // Ejecutar y verificar excepción
        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> clienteService.actualizar(1L, clienteNuevo),
                "Se esperaba una DuplicateResourceException si el correo ya existe"
        );

        assertEquals(Constants.ERR_DUPLICADO, ex.getMessage());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    /**
     * Caso negativo:
     * Debe lanzar una excepción si el cliente no existe.
     */
    @Test
    void actualizarCliente_DeberiaLanzarExcepcionSiNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> clienteService.actualizar(99L, clienteNuevo),
                "Se esperaba una ResourceNotFoundException si el ID no existe"
        );

        assertTrue(ex.getMessage().contains("no encontrado"));
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    /**
     * Caso positivo:
     * Debe actualizar correctamente cuando no hay duplicados.
     */
    @Test
    void actualizarCliente_DeberiaActualizarCorrectamente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.existsByCorreo("nuevo@correo.com")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteNuevo);

        Cliente resultado = clienteService.actualizar(1L, clienteNuevo);

        assertNotNull(resultado);
        assertEquals("nuevo@correo.com", resultado.getCorreo());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }
}
