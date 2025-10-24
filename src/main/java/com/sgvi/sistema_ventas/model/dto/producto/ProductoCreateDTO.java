package com.sgvi.sistema_ventas.model.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.sgvi.sistema_ventas.model.enums.Genero;
import java.math.BigDecimal;
import jakarta.validation.constraints.*;

/**
 * DTO para la creación de un nuevo Producto.
 * Incluye validaciones básicas de campos obligatorios y rangos numéricos.
 *
 * @author Wilian Lopez
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoCreateDTO {

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 50, message = "El código no puede exceder los 50 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;

    @Size(max = 50, message = "La marca no puede exceder los 50 caracteres")
    private String marca;

    @Size(max = 10, message = "La talla no puede exceder los 10 caracteres")
    private String talla;

    @Size(max = 30, message = "El color no puede exceder los 30 caracteres")
    private String color;

    @Size(max = 50, message = "El material no puede exceder los 50 caracteres")
    private String material;

    @NotNull(message = "El género es obligatorio")
    private Genero genero;

    @NotNull(message = "El precio de compra es obligatorio")
    @DecimalMin(value = "0.00", inclusive = true, message = "El precio de compra no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "Formato de precio de compra inválido (máx 8 enteros, 2 decimales)")
    private BigDecimal precioCompra;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.00", inclusive = true, message = "El precio de venta no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "Formato de precio de venta inválido (máx 8 enteros, 2 decimales)")
    private BigDecimal precioVenta;

    @NotNull(message = "El stock inicial es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    private String descripcion;

    @Size(max = 255, message = "La URL de la imagen no puede exceder los 255 caracteres")
    private String imagenUrl;

    @NotNull(message = "La categoría es obligatoria")
    @Positive(message = "El ID de la categoría debe ser positivo")
    private Long idCategoria;

    @Positive(message = "El ID del proveedor debe ser positivo")
    private Long idProveedor;
}