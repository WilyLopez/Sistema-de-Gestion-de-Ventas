package com.sgvi.sistema_ventas.model.dto.rol;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para asignar permisos a un rol.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignarPermisosRequest {

    @NotEmpty(message = "Debe proporcionar al menos un permiso")
    private List<@NotNull(message = "Los IDs de permisos no pueden ser nulos") Long> idsPermisos;
}