package com.sgvi.sistema_ventas.Service;

import com.sgvi.sistema_ventas.exception.DuplicateResourceException;
import com.sgvi.sistema_ventas.model.entity.Rol;
import com.sgvi.sistema_ventas.repository.PermisoRepository;
import com.sgvi.sistema_ventas.repository.RolPermisoRepository;
import com.sgvi.sistema_ventas.repository.RolRepository;
import com.sgvi.sistema_ventas.service.impl.RolServiceImpl;
import jakarta.validation.ValidationException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class RolServiceImplTest {

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PermisoRepository permisoRepository;

    @Mock
    private RolPermisoRepository rolPermisoRepository;

    private RolServiceImpl rolService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        rolService = new RolServiceImpl(rolRepository, permisoRepository, rolPermisoRepository);
    }

    // ✅ Caso 1: Crear rol correctamente
    @Test
    public void crear_DeberiaCrearRolCorrectamente() {
        // Arrange
        Rol rol = new Rol();
        rol.setNombre("Supervisor");
        rol.setDescripcion("Rol con permisos intermedios");
        rol.setNivelAcceso(5);

        Rol rolGuardado = new Rol();
        rolGuardado.setIdRol(1L);
        rolGuardado.setNombre("Supervisor");
        rolGuardado.setDescripcion("Rol con permisos intermedios");
        rolGuardado.setNivelAcceso(5);
        rolGuardado.setEstado(true);
        rolGuardado.setFechaCreacion(LocalDateTime.now());

        when(rolRepository.existsByNombre("Supervisor")).thenReturn(false);
        when(rolRepository.save(any(Rol.class))).thenReturn(rolGuardado);

        // Act
        Rol resultado = rolService.crear(rol);

        // Assert
        assertNotNull(resultado);
        assertEquals(resultado.getNombre(), "Supervisor");
        assertTrue(resultado.getEstado());
        assertNotNull(resultado.getFechaCreacion());
        verify(rolRepository, times(1)).save(any(Rol.class));
    }

    // ⚠️ Caso 2: Nombre duplicado → debe lanzar DuplicateResourceException
    @Test(expectedExceptions = DuplicateResourceException.class)
    public void crear_DeberiaLanzarExcepcionSiNombreDuplicado() {
        // Arrange
        Rol rol = new Rol();
        rol.setNombre("Administrador");
        rol.setNivelAcceso(5);

        when(rolRepository.existsByNombre("Administrador")).thenReturn(true);

        // Act
        rolService.crear(rol);
    }

    // 🚫 Caso 3: Nivel de acceso inválido → debe lanzar ValidationException
    @Test(expectedExceptions = ValidationException.class)
    public void crear_DeberiaLanzarExcepcionSiNivelAccesoInvalido() {
        // Arrange
        Rol rol = new Rol();
        rol.setNombre("NuevoRol");
        rol.setNivelAcceso(20); // fuera del rango válido

        when(rolRepository.existsByNombre("NuevoRol")).thenReturn(false);

        // Act
        rolService.crear(rol);
    }
}
