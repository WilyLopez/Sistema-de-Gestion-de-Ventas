package com.sgvi.sistema_ventas.Service;

import com.sgvi.sistema_ventas.model.entity.Rol;
import com.sgvi.sistema_ventas.model.entity.Usuario;
import com.sgvi.sistema_ventas.repository.UsuarioRepository;
import com.sgvi.sistema_ventas.security.UserPrincipal;
import com.sgvi.sistema_ventas.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el método loadUserById() de CustomUserDetailsService.
 * Tema: tienda de ropa - gestión de usuarios y autenticación.
 *
 * Este test verifica que:
 *  - Se cargue correctamente un usuario existente por su ID.
 *  - Se lance una excepción si el usuario no existe.
 */
@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setCorreo("jhamil@example.com");
        usuario.setContrasena("123456");
        usuario.setNombre("Jhamil");
        usuario.setApellido("Suarez");

        Rol rol = new Rol();
        rol.setNombre("ROLE_USER");
        usuario.setRol(rol);
    }

    /**
     * Caso positivo:
     * Debe retornar un UserDetails válido cuando el usuario existe.
     */
    @Test
    void loadUserById_DeberiaRetornarUserDetailsSiExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        UserDetails userDetails = customUserDetailsService.loadUserById(1L);

        assertNotNull(userDetails, "El UserDetails no debe ser nulo.");
        assertEquals("jhamil@example.com", userDetails.getUsername());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    /**
     * Caso negativo:
     * Debe lanzar UsernameNotFoundException si el usuario no existe.
     */
    @Test
    void loadUserById_DeberiaLanzarExcepcionSiNoExiste() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserById(99L),
                "Se esperaba UsernameNotFoundException si el usuario no existe"
        );

        assertTrue(ex.getMessage().contains("Usuario no encontrado"));
        verify(usuarioRepository, times(1)).findById(99L);
    }
}
