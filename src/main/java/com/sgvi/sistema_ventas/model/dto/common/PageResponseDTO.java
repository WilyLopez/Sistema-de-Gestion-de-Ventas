package com.sgvi.sistema_ventas.model.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO genérico para la respuesta de colecciones de datos paginadas.
 * @param <T> El tipo de elemento contenido en la página.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponseDTO<T> {

    /** Contenido de la página actual. */
    private List<T> content;

    /** Número total de elementos en el resultado completo. */
    private long totalElements;

    /** Número total de páginas disponibles. */
    private int totalPages;

    /** Índice de la página actual (base 0). */
    private int currentPage;

    /** Tamaño de la página. */
    private int pageSize;

    /** Indica si es la primera página. */
    private boolean isFirst;

    /** Indica si es la última página. */
    private boolean isLast;
}