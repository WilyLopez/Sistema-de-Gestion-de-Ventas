package com.sgvi.sistema_ventas.model.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.sgvi.sistema_ventas.model.enums.Genero;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * DTO para definir los criterios de búsqueda y filtrado de Productos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoBusquedaDTO {

    /** Palabra clave para buscar en nombre, código o descripción. */
    @Size(max = 100, message = "La palabra clave es demasiado larga.")
    private String keyword;

    /** Filtro por ID de categoría. */
    @Min(value = 1, message = "ID de categoría inválido.")
    private Long idCategoria;

    /** Filtro por ID de proveedor. */
    @Min(value = 1, message = "ID de proveedor inválido.")
    private Long idProveedor;

    /** Filtro por marca. */
    @Size(max = 50, message = "La marca es demasiado larga.")
    private String marca;

    /** Filtro por género. */
    private Genero genero;

    /** Filtro por talla. */
    private String talla;

    /**
     * Filtro para buscar productos cuyo stock sea inferior o igual al mínimo.
     * Si es true, filtra por stock <= StockMinimo.
     */
    private Boolean stockBajo;

    /** Filtro por estado (true=Activo, false=Inactivo). */
    private Boolean estado;
}