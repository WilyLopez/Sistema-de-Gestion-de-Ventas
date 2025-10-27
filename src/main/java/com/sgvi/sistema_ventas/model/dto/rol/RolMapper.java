package com.sgvi.sistema_ventas.model.dto.rol;

import com.sgvi.sistema_ventas.model.entity.Permiso;
import com.sgvi.sistema_ventas.model.entity.Rol;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper para convertir entidades Rol a DTOs.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Component
public class RolMapper {

    /**
     * Convierte Rol a RolDTO (sin permisos).
     */
    public RolDTO toDTO(Rol rol) {
        if (rol == null) {
            return null;
        }

        return RolDTO.builder()
                .idRol(rol.getIdRol())
                .nombre(rol.getNombre())
                .descripcion(rol.getDescripcion())
                .nivelAcceso(rol.getNivelAcceso())
                .estado(rol.getEstado())
                .fechaCreacion(rol.getFechaCreacion())
                .build();
    }

    /**
     * Convierte Rol a RolConPermisosDTO (incluye permisos).
     * IMPORTANTE: Solo usar cuando los permisos estén cargados (fetch eager o transacción activa).
     */
    public RolConPermisosDTO toDTOConPermisos(Rol rol) {
        if (rol == null) {
            return null;
        }

        return RolConPermisosDTO.builder()
                .idRol(rol.getIdRol())
                .nombre(rol.getNombre())
                .descripcion(rol.getDescripcion())
                .nivelAcceso(rol.getNivelAcceso())
                .estado(rol.getEstado())
                .fechaCreacion(rol.getFechaCreacion())
                .permisos(
                        rol.getPermisos() != null
                                ? rol.getPermisos().stream()
                                .map(this::permisoToDTO)
                                .collect(Collectors.toSet())
                                : null
                )
                .build();
    }

    /**
     * Convierte Permiso a PermisoDTO.
     */
    private PermisoDTO permisoToDTO(Permiso permiso) {
        if (permiso == null) {
            return null;
        }

        return PermisoDTO.builder()
                .idPermiso(permiso.getIdPermiso())
                .nombre(permiso.getNombre())
                .descripcion(permiso.getDescripcion())
                .categoria(permiso.getModulo())
                .build();
    }
}