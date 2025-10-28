package com.sgvi.sistema_ventas.model.dto.rol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuestas de Rol (sin permisos).
 * Evita problemas de serialización LazyInitialization.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolDTO {
    private Long idRol;
    private String nombre;
    private String descripcion;
    private Integer nivelAcceso;
    private Boolean estado;
    private LocalDateTime fechaCreacion;
}