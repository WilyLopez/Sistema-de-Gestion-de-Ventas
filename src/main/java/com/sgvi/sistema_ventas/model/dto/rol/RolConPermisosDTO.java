package com.sgvi.sistema_ventas.model.dto.rol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO para Rol con sus permisos incluidos.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolConPermisosDTO {
    private Long idRol;
    private String nombre;
    private String descripcion;
    private Integer nivelAcceso;
    private Boolean estado;
    private LocalDateTime fechaCreacion;
    private Set<com.sgvi.sistema_ventas.model.dto.rol.PermisoDTO> permisos;
}