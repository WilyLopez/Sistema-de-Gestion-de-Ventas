package com.sgvi.sistema_ventas.model.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Estructura DTO para detallar un error específico, típicamente usado en validaciones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorDTO {

    /** Campo o propiedad donde ocurrió el error. Puede ser nulo para errores generales. */
    private String field;

    /** Mensaje de error asociado a ese campo. */
    private String message;
}