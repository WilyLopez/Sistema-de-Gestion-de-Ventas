package com.sgvi.sistema_ventas.Service;

import com.sgvi.sistema_ventas.exception.ResourceNotFoundException;
import com.sgvi.sistema_ventas.model.entity.Usuario;
import com.sgvi.sistema_ventas.repository.UsuarioRepository;
import com.sgvi.sistema_ventas.service.impl.UsuarioServiceImpl;
import com.sgvi.sistema_ventas.util.validation.EmailValidator;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private EmailValidator emailValidator;

    private UsuarioServiceImpl usuarioService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        usuarioService = new UsuarioServiceImpl(usuarioRepository, passwordEncoder, emailValidator);
    }

    // Caso 1: Activar usuario correctamente
    @Test
    public void activar_DeberiaActivarUsuarioCorrectamente() {
        // Arrange
        Long idUsuario = 1L;
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setEstado(false); // inicialmente inactivo

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        usuarioService.activar(idUsuario);

        // Assert
        assertTrue(usuario.getEstado(), "El usuario debería haberse activado");
        assertNotNull(usuario.getFechaActualizacion(), "Debe tener una fecha de actualización");
        verify(usuarioRepository, times(1)).save(usuario);
    }

    // ⚠️ Caso 2: Usuario no encontrado → lanza excepción
    @Test(expectedExceptions = ResourceNotFoundException.class)
    public void activar_DeberiaLanzarExcepcionSiUsuarioNoExiste() {
        // Arrange
        Long idInexistente = 99L;
        when(usuarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Act
        usuarioService.activar(idInexistente);
    }
}
