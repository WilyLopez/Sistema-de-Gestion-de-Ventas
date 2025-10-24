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
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoBusquedaDTO {

    @Size(max = 100, message = "La palabra clave es demasiado larga")
    private String keyword;

    @Min(value = 1, message = "ID de categoría inválido")
    private Long idCategoria;

    @Min(value = 1, message = "ID de proveedor inválido")
    private Long idProveedor;

    @Size(max = 50, message = "La marca es demasiado larga")
    private String marca;

    private Genero genero;

    private String talla;

    private Boolean stockBajo;

    private Boolean estado;
}