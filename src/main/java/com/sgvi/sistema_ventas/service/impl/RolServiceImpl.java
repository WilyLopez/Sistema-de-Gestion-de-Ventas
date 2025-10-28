package com.sgvi.sistema_ventas.service.impl;

import com.sgvi.sistema_ventas.exception.BusinessException;
import com.sgvi.sistema_ventas.exception.DuplicateResourceException;
import com.sgvi.sistema_ventas.exception.ResourceNotFoundException;
import com.sgvi.sistema_ventas.model.entity.Rol;
import com.sgvi.sistema_ventas.model.entity.Permiso;
import com.sgvi.sistema_ventas.model.entity.RolPermiso;
import com.sgvi.sistema_ventas.repository.RolRepository;
import com.sgvi.sistema_ventas.repository.PermisoRepository;
import com.sgvi.sistema_ventas.repository.RolPermisoRepository;
import com.sgvi.sistema_ventas.service.interfaces.IRolService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementación del servicio de gestión de roles.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RolServiceImpl implements IRolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final RolPermisoRepository rolPermisoRepository;

    @Override
    public Rol crear(Rol rol) {
        log.info("Creando rol: {}", rol.getNombre());

        validarRolNuevo(rol);

        rol.setEstado(true);
        rol.setFechaCreacion(LocalDateTime.now());

        Rol rolCreado = rolRepository.save(rol);
        log.info("Rol creado exitosamente con ID: {}", rolCreado.getIdRol());

        return rolCreado;
    }

    @Override
    @Transactional(readOnly = true)
    public Rol obtenerPorIdConPermisos(Long id) {
        Rol rol = obtenerPorId(id);
        // Forzar la carga de permisos mientras la transacción está activa
        if (rol.getPermisos() != null) {
            rol.getPermisos().size(); // Esto inicializa la colección lazy
        }
        return rol;
    }

    @Override
    public Rol actualizar(Long id, Rol rol) {
        log.info("Actualizando rol con ID: {}", id);

        Rol rolExistente = obtenerPorId(id);

        // Validar nombre si cambió
        if (!rolExistente.getNombre().equals(rol.getNombre())
                && existePorNombre(rol.getNombre())) {
            throw new DuplicateResourceException("El nombre del rol ya existe: " + rol.getNombre());
        }

        rolExistente.setNombre(rol.getNombre());
        rolExistente.setDescripcion(rol.getDescripcion());
        rolExistente.setNivelAcceso(rol.getNivelAcceso());

        Rol rolActualizado = rolRepository.save(rolExistente);
        log.info("Rol actualizado exitosamente: {}", id);

        return rolActualizado;
    }

    @Override
    public void eliminar(Long id) {
        log.info("Eliminando rol con ID: {}", id);

        Rol rol = obtenerPorId(id);

        // No permitir eliminar roles del sistema
        if (rol.getNombre().equals("administrador") ||
                rol.getNombre().equals("vendedor") ||
                rol.getNombre().equals("empleado")) {
            throw new BusinessException("No se pueden eliminar roles del sistema");
        }

        rol.setEstado(false);
        rolRepository.save(rol);

        log.info("Rol eliminado exitosamente: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Rol obtenerPorId(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Rol obtenerPorNombre(String nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + nombre));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Rol> listarTodos(Pageable pageable) {
        return rolRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rol> listarActivos() {
        return rolRepository.findByEstado(true);
    }

    @Override
    public void asignarPermisos(Long idRol, List<Long> idsPermisos) {
        log.info("Asignando {} permisos al rol ID: {}", idsPermisos.size(), idRol);

        Rol rol = obtenerPorId(idRol);

        // Limpiar permisos existentes
        rolPermisoRepository.deleteByIdRol(idRol);

        // Asignar nuevos permisos
        for (Long idPermiso : idsPermisos) {
            Permiso permiso = permisoRepository.findById(idPermiso)
                    .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado: " + idPermiso));

            RolPermiso rolPermiso = RolPermiso.builder()
                    .idRol(idRol)
                    .idPermiso(idPermiso)
                    .build();

            rolPermisoRepository.save(rolPermiso);
        }

        log.info("Permisos asignados exitosamente");
    }

    @Override
    public void removerPermiso(Long idRol, Long idPermiso) {
        log.info("Removiendo permiso {} del rol {}", idPermiso, idRol);

        rolPermisoRepository.deleteByIdRolAndIdPermiso(idRol, idPermiso);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Permiso> obtenerPermisos(Long idRol) {
        Rol rol = obtenerPorId(idRol);
        return rol.getPermisos() != null ? rol.getPermisos() : new HashSet<>();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean tienePermiso(Long idRol, String nombrePermiso) {
        Set<Permiso> permisos = obtenerPermisos(idRol);
        return permisos.stream()
                .anyMatch(p -> p.getNombre().equals(nombrePermiso));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        return rolRepository.existsByNombre(nombre);
    }

    // ========== MÉTODOS PRIVADOS ==========

    private void validarRolNuevo(Rol rol) {
        if (rol.getNombre() == null || rol.getNombre().trim().isEmpty()) {
            throw new ValidationException("El nombre del rol es obligatorio");
        }

        if (existePorNombre(rol.getNombre())) {
            throw new DuplicateResourceException("El nombre del rol ya existe: " + rol.getNombre());
        }

        if (rol.getNivelAcceso() == null || rol.getNivelAcceso() < 1 || rol.getNivelAcceso() > 10) {
            throw new ValidationException("El nivel de acceso debe estar entre 1 y 10");
        }
    }
}